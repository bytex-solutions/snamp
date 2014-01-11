package com.snamp.adapters;

import com.snamp.connectors.*;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.request.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import static com.snamp.adapters.SnmpHelpers.getAccessRestrictions;
import static com.snamp.connectors.util.ManagementEntityTypeHelper.*;

import com.snamp.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Represents SNMP table.
 * @author Roman Sakno
 */
final class SnmpTableObject extends DefaultMOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> implements SnmpAttributeMapping, UpdatableManagedObject{

    /**
     * Represents transaction state.
     */
    private static enum TransactionState{
        PREPARE((byte)0),
        COMMIT((byte)1),
        ROLLBACK((byte)2),
        CLEANUP((byte)3);

        private final byte stateId;

        private TransactionState(final byte sid){
            this.stateId = sid;
        }

        private boolean isValidTransition(final byte nextState){
            switch (nextState){
                case 0: return nextState == 0 || nextState == 1 || nextState == 2;
                case 1: return nextState == 1 || nextState == 3;
                case 2: return nextState == 2 || nextState == 3;
                case 3: return nextState == 3;
                default: return false;
            }
        }

        public final boolean isValidTransition(final TransactionState nextState){
            return isValidTransition(nextState.stateId);
        }
    }

    private static enum TransactionCompletionState{
        IN_PROGRESS,
        SUCCESS,
        ROLLED_BACK
    }

    /**
     * Represents information about SNMP transaction. This class cannot be inherited.
     */
    private static final class TransactionInfo{
        private static final String TRANSACTION_INFO_HOLDER = "snampTransactionInfo";
        private TransactionState state;

        /**
         * Represents transaction identifier.
         */
        public final long transactionId;

        //state pending counters
        private volatile long prepareCounter;
        private volatile long commitCounter;
        private volatile long rollbackCounter;
        private volatile long cleanupCounter;

        /**
         * Initializes a new instance of the transaction descriptor.
         */
        public TransactionInfo(final long transactionId){
            state = null;
            this.transactionId = transactionId;
            prepareCounter = commitCounter = rollbackCounter = cleanupCounter = 0L;
        }

        /**
         * Returns the state of this transaction.
         * @return The state of this transaction.
         */
        public final TransactionState getState(){
            return state;
        }

        /**
         * Changes state of this transaction.
         * @param newState A new state of this transaction.
         * @throws IllegalArgumentException Invalid new state for this transaction.
         */
        public final void setState(final TransactionState newState) throws IllegalArgumentException{
            if(newState == null) throw new IllegalArgumentException("newState is null.");
            else if(state == null)
                switch (newState){
                    case PREPARE:
                    case COMMIT:
                    case ROLLBACK:
                        state = newState;
                    break;
                    default: throw new IllegalArgumentException(String.format("Invalid transaction state %s", newState));
                }
            else if(state.isValidTransition(newState))
                state = newState;
            else throw new IllegalArgumentException(String.format("Invalid transaction state transition from %s to %s.", state, newState));
            pending();
        }

        public final void pending(){
            if(state == null) return;
            switch (state){
                case PREPARE: prepareCounter += 1; return;
                case COMMIT: commitCounter += 1; return;
                case ROLLBACK: rollbackCounter += 1; return;
                case CLEANUP: cleanupCounter += 1; return;
            }
        }

        public final boolean isCommitCompleted(){
            return commitCounter >= prepareCounter;
        }

        public final boolean isRollbackCompleted(){
            return rollbackCounter >= prepareCounter;
        }

        public final TransactionCompletionState getCompletionState(){
            if(rollbackCounter > 0)
                return cleanupCounter >= rollbackCounter ? TransactionCompletionState.ROLLED_BACK : TransactionCompletionState.IN_PROGRESS;
            else if(commitCounter > 0)
                return cleanupCounter >= commitCounter ? TransactionCompletionState.SUCCESS : TransactionCompletionState.IN_PROGRESS;
            else return TransactionCompletionState.IN_PROGRESS;
        }

        private static TransactionInfo pendingState(final Request<?, ?> request, final TransactionState state){
            final TransactionInfo info;
            final Object untypedInfo = request.getProcessingUserObject(TRANSACTION_INFO_HOLDER);
            if(untypedInfo instanceof TransactionInfo)
                info = (TransactionInfo)untypedInfo;
            else request.setProcessingUserObject(TRANSACTION_INFO_HOLDER, info = new TransactionInfo(request.getTransactionID()));
            synchronized (info){
                info.setState(state);
            }
            return info;
        }

        public static TransactionInfo pendingState(final SubRequest<?, ?> subreq, final TransactionState state){
            return pendingState(subreq.getRequest(), state);
        }
    }

    private static final class UpdateManager extends CountdownTimer{
        public final TimeSpan tableCacheTime;
        private Object updateSource;
        private Date updateTimeStamp;

        public UpdateManager(final TimeSpan initial){
            super(initial);
            this.tableCacheTime = initial;
        }

        public UpdateManager(){
            this(new TimeSpan(5, TimeUnit.SECONDS));
        }

        public final void reset(){
            setTimerValue(tableCacheTime);
        }

        public final void updateCompleted(final Object source){
            this.updateSource = source;
            this.updateTimeStamp = new Date();
        }

        public final Object getUpdateSource(){
            return updateSource;
        }

        public final Date getUpdateTimeStamp(){
            return updateTimeStamp;
        }
    }

    private final AttributeSupport _connector;
    private final AttributeMetadata attributeInfo;
    private final TimeSpan readWriteTimeout;
    private static final String TABLE_CACHE_TIME_PARAM = "tableCacheTime";
    private static final String USE_ROW_STATUS_PARAM = "useRowStatus";
    private final UpdateManager cacheManager;
    private final Map<String, String> conversionOptions;
    private final boolean useRowStatus;

    private static MONamedColumn<Variable>[] createColumns(final ManagementEntityTabularType tableType, final MOAccess access){
        int columnID = 2;
        if(ManagementEntityTypeBuilder.isArray(tableType)) //hides column with array indexes
            return new MONamedColumn[]{new MONamedColumn<>(columnID, ManagementEntityTypeBuilder.AbstractManagementEntityArrayType.VALUE_COLUMN_NAME, tableType, access)};

        else {
            final List<MONamedColumn<Variable>> columns = new ArrayList<>(tableType.getColumns().size());
            for(final String columnName: tableType.getColumns())
                columns.add(new MONamedColumn<>(columnID++, columnName, tableType, access));
            return columns.toArray(new MONamedColumn[columns.size()]);
        }
    }

    private static OID makeRowID(final int rowIndex){
        return new OID(new int[]{rowIndex + 1});
    }

    private SnmpTableObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts, final AttributeMetadata attribute){
        super(new OID(oid),
                new MOTableIndex(new MOTableSubIndex[]{new MOTableSubIndex(null, SMIConstants.SYNTAX_INTEGER, 1, 1)}),
                createColumns((ManagementEntityTabularType)attribute.getType(), getAccessRestrictions(attribute, true))
        );
        //setup table model
        final DefaultMOMutableTableModel<MOTableRow<Variable>> tableModel = new DefaultMOMutableTableModel<>();
        tableModel.setRowFactory(new DefaultMOMutableRow2PCFactory());
        this.setModel(tableModel);
        //save additional fields
        _connector = connector;
        readWriteTimeout = timeouts;
        attributeInfo = attribute;
        conversionOptions = new HashMap<>(attribute);
        cacheManager = conversionOptions.containsKey(TABLE_CACHE_TIME_PARAM) ?
                new UpdateManager(new TimeSpan(Integer.valueOf(conversionOptions.get(TABLE_CACHE_TIME_PARAM)))):
                new UpdateManager();
        useRowStatus = conversionOptions.containsKey(USE_ROW_STATUS_PARAM) ?
                Boolean.valueOf(conversionOptions.get(USE_ROW_STATUS_PARAM)):
                false;
    }

    /**
     * Initializes a new SNMP table wrapper for the specified attribute.
     * @param oid
     * @param connector
     * @param timeouts
     */
    public SnmpTableObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts){
        this(oid, connector, timeouts, connector.getAttributeInfo(oid));
    }

    /**
     * Returns the metadata of the underlying attribute.
     *
     * @return The metadata of the underlying attribute.
     */
    @Override
    public final AttributeMetadata getMetadata() {
        return attributeInfo;
    }

    /**
     * Determines whether this table is empty.
     * @return {@literal true}, if this table is empty; otherwise, {@literal false}.
     */
    public final boolean isEmpty(){
        return getModel().getRowCount() == 0;
    }

    /**
     * Returns management type of this table.
     * @return The management type of this table.
     */
    public final ManagementEntityTabularType getTableType(){
        return (ManagementEntityTabularType)attributeInfo.getType();
    }

    private static Variable convert(final Object cellValue, final ManagementEntityType cellType, final Map<String, String> options){
        final SnmpType typeProvider = SnmpType.map(cellType);
        return typeProvider.convert(cellValue, cellType, options);
    }

    private static Object convert(final Variable cellValue, final ManagementEntityType cellType, final Map<String, String> options){
        final SnmpType typeProvider = SnmpType.map(cellType);
        return typeProvider.convert(cellValue, cellType, options);
    }

    private static Variable[] createRow(final int rowIndex, final Table<String> values, final MONamedColumn<Variable>[] columns, final ManagementEntityTabularType type, final Map<String, String> conversionOptions){
        final Variable[] result = new Variable[columns.length];
        for(int columnIndex = 0; columnIndex < result.length; columnIndex++){
            final MONamedColumn<Variable> columnDef = columns[columnIndex];
            final Variable cellValue = convert(values.getCell(columnDef.name, rowIndex), type.getColumnType(columnDef.name), conversionOptions);
            result[columnIndex] = cellValue;
        }
        return result;
    }

    private static void fill(final Table<String> values, final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table, final ManagementEntityTabularType type, final Map<String, String> conversionOptions){
        //create rows
        for(int rowIndex = 0; rowIndex < values.getRowCount(); rowIndex++){
            final OID rowID = makeRowID(rowIndex);
            final MOTableRow<Variable> newRow = table.createRow(rowID, createRow(rowIndex, values, table.getColumns(), type, conversionOptions));
            table.addRow(newRow);
        }
    }

    private static void fill(final Object[] values, final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table, final ManagementEntityTabularType type, final Map<String, String> conversionOptions){
        @Temporary
        final Table<String> tempTable = new SimpleTable<>(new HashMap<String, Class<?>>(1){{
            put(ManagementEntityTypeBuilder.ManagementEntityArrayType.VALUE_COLUMN_NAME, Object.class);
        }});
        for(int arrayIndex = 0; arrayIndex < values.length; arrayIndex++){
            @Temporary
            final int firstColumnValue = arrayIndex;
            tempTable.addRow(new HashMap<String, Object>(1){{
                put(ManagementEntityTypeBuilder.ManagementEntityArrayType.VALUE_COLUMN_NAME, values[firstColumnValue]);
            }});
        }
        fill(tempTable, table, type, conversionOptions);
    }

    private static Object fill(final AttributeSupport connector, final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table, final ManagementEntityTabularType type, final TimeSpan rwTimeout, final Map<String, String> conversionOptions) throws TimeoutException {
        final Object lastUpdateSource;
        if(supportsProjection(type, Table.class))
            fill(convertFrom(type, lastUpdateSource = connector.getAttribute(table.getOID().toString(), rwTimeout, new SimpleTable<String>()), Table.class), table, type, conversionOptions);
        else if(supportsProjection(type, Object[].class) && ManagementEntityTypeBuilder.isArray(type))
            fill(convertFrom(type, lastUpdateSource = connector.getAttribute(table.getOID().toString(), rwTimeout, new Object[0]), Object[].class), table, type, conversionOptions);
        else {
            log.warning(String.format("Source attribute table %s is not supported", table.getOID()));
            lastUpdateSource = null;
        }
        return lastUpdateSource;
    }

    private void fillTableIfNecessary() throws TimeoutException {
        if(isEmpty()) try{
            cacheManager.updateCompleted(fill(_connector, this, getTableType(), readWriteTimeout, conversionOptions));
        }
        finally {
            cacheManager.stop();
            cacheManager.reset();
            cacheManager.start();
        }
        else {
            cacheManager.stop();
            try{
                if(cacheManager.isEmpty()){
                    removeAll();
                    cacheManager.updateCompleted(fill(_connector, this, getTableType(), readWriteTimeout, conversionOptions));
                    cacheManager.reset();
                }
            }
            finally{
                cacheManager.start();
            }
        }
    }

    /**
     * Gets the date and time of the last update. If that time cannot be
     * determined <code>null</code> is returned.
     *
     * @return the Date when the last {@link #update(org.snmp4j.agent.MOQuery updateScope)} has
     * been called.
     */
    @Override
    public final Date getLastUpdate() {
        return cacheManager.getUpdateTimeStamp();
    }

    /**
     * Gets the object that triggered the last update of this managed object.
     * The returned object reference may be used to check if an update has
     * already been performed for the specified source, which is typically a
     * SNMP request.
     *
     * @return an object or <code>null</code> if the source of the last update is
     * unknown/undefined.
     */
    @Override
    public final Object getLastUpdateSource() {
        return cacheManager.getUpdateSource();
    }

    /**
     * Update the content of the managed object that is covered by the supplied
     * scope.
     *
     * @param updateScope the query that triggered the update and thus defining the update scope.
     *                    If <code>null</code> the whole managed object has to be updated.
     */
    @Override
    public final void update(final MOQuery updateScope) {
        update((MOScope)updateScope);
    }

    @Override
    public final void update(final MOScope updateScope){
        try {
            fillTableIfNecessary();
        }
        catch (final TimeoutException e) {
            log.log(Level.SEVERE, "Unable to update SNMP table.", e);
        }
    }

    private void dumpArray(final ManagementEntityType elementType) throws TimeoutException {
        final MOTableModel<MOTableRow<Variable>> model = getModel();
        final Object[] array = new Object[model.getRowCount()];
        for(int i = 0; i < model.getRowCount(); i++){
            final MOTableRow<Variable> row = model.getRow(makeRowID(i));
            if(row == null){ //cancels row sending
                log.severe(String.format("Row %s is null. Sending array is cancelled", makeRowID(i)));
                return;
            }
            array[i] = convert(row.getValue(0), elementType, conversionOptions);
        }
        _connector.setAttribute(getID().toString(), readWriteTimeout, array);
    }

    //this method is synchronized because sending table rows to connector is atomic
    private synchronized void dumpTable() throws TimeoutException {
        if(ManagementEntityTypeBuilder.isArray(getTableType()))
            dumpArray(getTableType().getColumnType(ManagementEntityTypeBuilder.AbstractManagementEntityArrayType.VALUE_COLUMN_NAME));
        else {
            final Table<String> table = new SimpleTable<>(new HashMap<String, Class<?>>(model.getColumnCount()){{
                for(int i = 0; i < getColumnCount(); i++)
                    put(getColumn(i).name, Object.class);
            }});
            for(int i = 0; i < model.getRowCount(); i++){
                final MOTableRow<Variable> row = model.getRow(makeRowID(i));
                if(row == null){ //cancels row sending
                    log.severe(String.format("Row %s is null. Sending table is cancelled", makeRowID(i)));
                    return;
                }
                table.addRow(new HashMap<String, Object>(getColumnCount()){{
                    for(int i = 0; i < getColumnCount(); i++){
                        final MONamedColumn<Variable> column = getColumn(i);
                        put(column.name, convert(row.getValue(i), getTableType().getColumnType(column.name), conversionOptions));
                    }
                }});
            }
            _connector.setAttribute(getID().toString(), readWriteTimeout, table);
        }
    }

    @Override
    public final void prepare(final SubRequest request) {
        TransactionInfo.pendingState(request, TransactionState.PREPARE);
        super.prepare(request);
    }

    @Override
    public final void commit(final SubRequest request) {
        TransactionInfo.pendingState(request, TransactionState.COMMIT);
        super.commit(request);
    }

    @Override
    public final void undo(final SubRequest request) {
        TransactionInfo.pendingState(request, TransactionState.ROLLBACK);
        super.undo(request);
    }

    @Override
    public final void cleanup(final SubRequest request) {
        final TransactionInfo info = TransactionInfo.pendingState(request, TransactionState.CLEANUP);
        switch (info.getCompletionState()){
            case SUCCESS:
                try {
                    dumpTable();
                }
                catch (final TimeoutException e) {
                    log.log(Level.SEVERE, "Timeout when sending table to management connector.", e);
                    request.setErrorStatus(SnmpConstants.SNMP_ERROR_COMMIT_FAILED);
                    request.completed();
                    return;
                }
                break;
            case ROLLED_BACK: log.severe(String.format("Transaction for table %s aborted.", this)); break;
        }
        super.cleanup(request);
    }
}
