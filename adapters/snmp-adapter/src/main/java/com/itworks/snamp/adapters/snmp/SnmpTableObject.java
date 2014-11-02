package com.itworks.snamp.adapters.snmp;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.*;
import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityTypeBuilder;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.internal.CountdownTimer;
import com.itworks.snamp.internal.annotations.Temporary;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.MOQuery;
import org.snmp4j.agent.MOScope;
import org.snmp4j.agent.UpdatableManagedObject;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.request.Request;
import org.snmp4j.agent.request.SubRequest;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static com.itworks.snamp.adapters.snmp.SnmpHelpers.getAccessRestrictions;
import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.convertFrom;
import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.supportsProjection;

/**
 * Represents SNMP table.
 * @author Roman Sakno
 */
final class SnmpTableObject extends DefaultMOTable<MOMutableTableRow, MONamedColumn<Variable>, MOTableModel<MOMutableTableRow>> implements SnmpAttributeMapping, UpdatableManagedObject{

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
            switch (stateId){
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

        private volatile long commitCounter;
        private volatile long rollbackCounter;
        private volatile long cleanupCounter;

        /**
         * Initializes a new instance of the transaction descriptor.
         */
        public TransactionInfo(final long transactionId){
            state = null;
            this.transactionId = transactionId;
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
                case COMMIT: commitCounter += 1; return;
                case ROLLBACK: rollbackCounter += 1; return;
                case CLEANUP: cleanupCounter += 1;
            }
        }

        public final TransactionCompletionState getCompletionState(){
            if(rollbackCounter > 0)
                return cleanupCounter >= rollbackCounter ? TransactionCompletionState.ROLLED_BACK : TransactionCompletionState.IN_PROGRESS;
            else if(commitCounter > 0)
                return cleanupCounter >= commitCounter ? TransactionCompletionState.SUCCESS : TransactionCompletionState.IN_PROGRESS;
            else return TransactionCompletionState.IN_PROGRESS;
        }

        @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
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

    private static final class UpdateManager extends CountdownTimer {
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

    private final AttributeAccessor _connector;
    private static final String TABLE_CACHE_TIME_PARAM = "tableCacheTime";
    private static final String USE_ROW_STATUS_PARAM = "useRowStatus";
    private final UpdateManager cacheManager;
    private final boolean useRowStatus;

    @SuppressWarnings("unchecked")
    private static MONamedColumn<Variable>[] createColumns(final ManagedEntityTabularType tableType, final MOAccess access, final boolean useRowStatus){
        int columnID = 2;
        final List<MONamedColumn<? extends Variable>> columns = new ArrayList<>(tableType.getColumns().size() + 1);
        if(ManagedEntityTypeBuilder.isArray(tableType)) //hides column with array indexes
            columns.add(new MONamedColumn<>(columnID++, ManagedEntityTypeBuilder.AbstractManagedEntityArrayType.VALUE_COLUMN_NAME, tableType, access));
        else {
            for(final String columnName: tableType.getColumns())
                columns.add(new MONamedColumn<>(columnID++, columnName, tableType, access));
            return columns.toArray(new MONamedColumn[columns.size()]);
        }
        //add RowStatus column
        if(useRowStatus)
            columns.add(new MORowStatusColumn(columnID));
        return columns.toArray(new MONamedColumn[columns.size()]);
    }

    private static OID makeRowID(final int rowIndex){
        return new OID(new int[]{rowIndex + 1});
    }

    private static boolean shouldUseRowStatus(final Map<String, String> options){
        return options.containsKey(USE_ROW_STATUS_PARAM) &&
                Boolean.valueOf(options.get(USE_ROW_STATUS_PARAM));
    }

    public SnmpTableObject(final String oid, final AttributeAccessor connector){
        super(new OID(oid),
                new MOTableIndex(new MOTableSubIndex[]{new MOTableSubIndex(null, SMIConstants.SYNTAX_INTEGER, 1, 1)}),
                createColumns((ManagedEntityTabularType)connector.getType(), getAccessRestrictions(connector, true), shouldUseRowStatus(connector))
        );
        //setup table model
        final DefaultMOMutableTableModel<MOMutableTableRow> tableModel = new DefaultMOMutableTableModel<>();
        tableModel.setRowFactory(new DefaultMOMutableRow2PCFactory());
        this.setModel(tableModel);
        //save additional fields
        _connector = connector;
        cacheManager = connector.containsKey(TABLE_CACHE_TIME_PARAM) ?
                new UpdateManager(new TimeSpan(Integer.valueOf(connector.get(TABLE_CACHE_TIME_PARAM)))):
                new UpdateManager();
        useRowStatus = shouldUseRowStatus(connector);
    }

    /**
     * Returns the metadata of the underlying attribute.
     *
     * @return The metadata of the underlying attribute.
     */
    @Override
    public final AttributeMetadata getMetadata() {
        return _connector;
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
    public final ManagedEntityTabularType getTableType(){
        return (ManagedEntityTabularType)_connector.getType();
    }


    private static Variable[] createRow(final int rowIndex, final Table<String> values, final MONamedColumn<Variable>[] columns, final ManagedEntityTabularType type, final Map<String, String> conversionOptions){
        final Variable[] result = new Variable[columns.length];
        for(int columnIndex = 0; columnIndex < result.length; columnIndex++){
            final MONamedColumn<Variable> columnDef = columns[columnIndex];
            result[columnIndex] = columnDef.createCellValue(values.getCell(columnDef.name, rowIndex), type.getColumnType(columnDef.name), conversionOptions);
        }
        return result;
    }

    private static void fill(final Table<String> values, final MOTable<MOMutableTableRow, MONamedColumn<Variable>, MOTableModel<MOMutableTableRow>> table, final ManagedEntityTabularType type, final Map<String, String> conversionOptions){
        //create rows
        for(int rowIndex = 0; rowIndex < values.getRowCount(); rowIndex++){
            final OID rowID = makeRowID(rowIndex);
            final MOMutableTableRow newRow = table.createRow(rowID, createRow(rowIndex, values, table.getColumns(), type, conversionOptions));
            table.addRow(newRow);
        }
    }

    private static void fill(final Object[] values, final MOTable<MOMutableTableRow, MONamedColumn<Variable>, MOTableModel<MOMutableTableRow>> table, final ManagedEntityTabularType type, final Map<String, String> conversionOptions){
        @Temporary
        final InMemoryTable<String> tempTable = new InMemoryTable<>(ManagedEntityTypeBuilder.ManagedEntityArrayType.VALUE_COLUMN_NAME,
                Object.class,
                values.length);
        for(int arrayIndex = 0; arrayIndex < values.length; arrayIndex++){
            @Temporary
            final int firstColumnValue = arrayIndex;
            final Map<String, Object> newRow = tempTable.newRow();
            newRow.put(ManagedEntityTypeBuilder.ManagedEntityArrayType.VALUE_COLUMN_NAME, values[firstColumnValue]);
            tempTable.addRow(newRow);
        }
        fill(tempTable, table, type, conversionOptions);
    }

    @SuppressWarnings("unchecked")
    private static Object fill(final AttributeAccessor connector, final MOTable<MOMutableTableRow, MONamedColumn<Variable>, MOTableModel<MOMutableTableRow>> table) throws TimeoutException, AttributeSupportException {
        final Object lastUpdateSource;
        if(supportsProjection(connector.getType(), TypeLiterals.STRING_COLUMN_TABLE))
            fill(convertFrom(connector.getType(), lastUpdateSource = connector.getRawValue(), TypeLiterals.STRING_COLUMN_TABLE), table, (ManagedEntityTabularType)connector.getType(), connector);
        else if(supportsProjection(connector.getType(), TypeLiterals.OBJECT_ARRAY) && ManagedEntityTypeBuilder.isArray(connector.getType()))
            fill(convertFrom(connector.getType(), lastUpdateSource = connector.getRawValue(), TypeLiterals.OBJECT_ARRAY), table, (ManagedEntityTabularType)connector.getType(), connector);
        else {
            log.warning(String.format("Source attribute table %s is not supported", table.getOID()));
            lastUpdateSource = null;
        }
        return lastUpdateSource;
    }

    private void fillTableIfNecessary() throws TimeoutException, AttributeSupportException {
        if(isEmpty()) try{
            cacheManager.updateCompleted(fill(_connector, this));
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
                    cacheManager.updateCompleted(fill(_connector, this));
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
        catch (final TimeoutException | AttributeSupportException e) {
            log.log(Level.SEVERE, "Unable to update SNMP table.", e);
        }
    }

    private Collection<OID> dumpArray(final ManagedEntityType elementType) throws TimeoutException, AttributeSupportException {
        final Collection<OID> rowsToDelete = new ArrayList<>(model.getRowCount());
        final int rowStatusIndex = useRowStatus ? SnmpHelpers.findColumnIndex(this, MORowStatusColumn.class) : -1;
        final Object[] array = new Object[model.getRowCount()];
        for(int i = 0; i < model.getRowCount(); i++){
            final MOMutableTableRow row = model.getRow(makeRowID(i));
            if(row == null){ //cancels row sending
                log.severe(String.format("Row %s is null. Sending array is cancelled", makeRowID(i)));
                return Collections.emptyList();
            }
            else if(rowStatusIndex >= 0)
                switch(TableRowStatus.parse(row.getValue(rowStatusIndex))){
                    case ACTIVE: break;
                    case CREATE_AND_GO:
                        row.setValue(rowStatusIndex, TableRowStatus.ACTIVE.toManagedScalarValue());
                        break;
                    case DESTROY:
                        rowsToDelete.add(row.getIndex());
                        continue;
                    default:
                        log.warning(String.format("Unsupported row status %s detected at row %s in table %s. Row is ignored.", row.getValue(rowStatusIndex), row.getIndex(), getOID()));
                    continue;
                }
            array[i] = getColumn(0).parseCellValue(row.getValue(0), elementType, _connector);
        }
        _connector.setValue(array);
        return rowsToDelete;
    }

    //this method is synchronized because sending table rows to connector is atomic
    private synchronized void dumpTable() throws TimeoutException, AttributeSupportException {
        final Collection<OID> rowsToDelete;
        if(ManagedEntityTypeBuilder.isArray(getTableType()))
            rowsToDelete = dumpArray(getTableType().getColumnType(ManagedEntityTypeBuilder.AbstractManagedEntityArrayType.VALUE_COLUMN_NAME));
        else {
            rowsToDelete = new ArrayList<>(model.getRowCount());
            final Table<String> table = new InMemoryTable<>(new SafeConsumer<ImmutableMap.Builder<String, Class<?>>>() {
                @Override
                public void accept(final ImmutableMap.Builder<String, Class<?>> output) {
                    for(int i = 0; i < getColumnCount(); i++){
                        final MONamedColumn<Variable> column = getColumn(i);
                        if(!column.isSynthetic()) output.put(column.name, Object.class);
                    }
                }
            },
                    model.getRowCount());
            for(int r = 0; r < model.getRowCount(); r++){
                final MOMutableTableRow row = model.getRow(makeRowID(r));
                if(row == null){ //cancels row sending
                    log.severe(String.format("Row %s is null. Sending table is cancelled", makeRowID(r)));
                    return;
                }
                final Map<String, Object> values = new HashMap<>(getColumnCount());
                boolean allowToAddRow = true;
                for(int c = 0; c < getColumnCount(); c++){
                    final MONamedColumn<Variable> column = getColumn(c);
                    if(column.isSynthetic()){
                        if(MORowStatusColumn.isInstance(column))
                            switch (TableRowStatus.parse(row.getValue(c))){
                                case CREATE_AND_GO:
                                    row.setValue(c, TableRowStatus.ACTIVE.toManagedScalarValue());
                                case ACTIVE:
                                    allowToAddRow = true;
                                    break;
                                case DESTROY:
                                    rowsToDelete.add(row.getIndex());
                                    allowToAddRow = false;
                                    break;
                                default:
                                    log.warning(String.format("Unsupported row status %s detected at row %s in table %s. Row is ignored.", row.getValue(c), row.getIndex(), getOID()));
                                    allowToAddRow = true;
                                    break;
                            }
                    }
                    else values.put(column.name, column.parseCellValue(row.getValue(c), getTableType().getColumnType(column.name), getMetadata()));
                }
                if(allowToAddRow) table.addRow(values);
            }
            _connector.setValue(table);
        }
        //remove rows
        for(final OID row: rowsToDelete)
            removeRow(row);
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
                catch (final TimeoutException | AttributeSupportException e) {
                    log.log(Level.SEVERE, "Unable to clean table", e);
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
