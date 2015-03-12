package com.itworks.snamp.adapters.snmp;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.TabularDataUtils;
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

import javax.management.Descriptor;
import javax.management.DescriptorRead;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import static com.itworks.snamp.adapters.snmp.SnmpHelpers.getAccessRestrictions;
import static com.itworks.snamp.jmx.DescriptorUtils.getField;
import static com.itworks.snamp.jmx.DescriptorUtils.hasField;

/**
 * Represents SNMP table.
 * @author Roman Sakno
 */
final class SnmpTableObject extends DefaultMOTable<DefaultMOMutableRow2PC, MONamedColumn, MOTableModel<DefaultMOMutableRow2PC>> implements SnmpAttributeMapping, UpdatableManagedObject{
    static final int SYNTAX = SMIConstants.EXCEPTION_NO_SUCH_OBJECT;

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
        private final long transactionId;

        private final AtomicLong commitCounter;
        private final AtomicLong rollbackCounter;
        private final AtomicLong cleanupCounter;

        /**
         * Initializes a new instance of the transaction descriptor.
         */
        private TransactionInfo(final long transactionId){
            state = null;
            this.transactionId = transactionId;
            commitCounter = new AtomicLong(0L);
            rollbackCounter = new AtomicLong(0L);
            cleanupCounter = new AtomicLong(0L);
        }

        /**
         * Changes state of this transaction.
         * @param newState A new state of this transaction.
         * @throws IllegalArgumentException Invalid new state for this transaction.
         */
        private synchronized void setState(final TransactionState newState) throws IllegalArgumentException{
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

        private void pending(){
            if(state == null) return;
            switch (state){
                case COMMIT: commitCounter.incrementAndGet(); return;
                case ROLLBACK: rollbackCounter.incrementAndGet(); return;
                case CLEANUP: cleanupCounter.incrementAndGet();
            }
        }

        private static TransactionCompletionState getCompletionState(final long rollbackCounter,
                                                                     final long commitCounter,
                                                                     final long cleanupCounter){
            if(rollbackCounter > 0)
                return cleanupCounter >= rollbackCounter ? TransactionCompletionState.ROLLED_BACK : TransactionCompletionState.IN_PROGRESS;
            else if(commitCounter > 0)
                return cleanupCounter >= commitCounter ? TransactionCompletionState.SUCCESS : TransactionCompletionState.IN_PROGRESS;
            else return TransactionCompletionState.IN_PROGRESS;
        }

        private TransactionCompletionState getCompletionState(){
            return getCompletionState(rollbackCounter.get(), commitCounter.get(), cleanupCounter.get());
        }

        private static TransactionInfo pendingState(final Request<?, ?, ?> request, final TransactionState state) {
            final TransactionInfo info;
            final Object untypedInfo = request.getProcessingUserObject(TRANSACTION_INFO_HOLDER);
            if (untypedInfo instanceof TransactionInfo)
                info = (TransactionInfo) untypedInfo;
            else
                request.setProcessingUserObject(TRANSACTION_INFO_HOLDER, info = new TransactionInfo(request.getTransactionID()));
            info.setState(state);
            return info;
        }

        private static TransactionInfo pendingState(final SubRequest subreq, final TransactionState state){
            return pendingState(subreq.getRequest(), state);
        }

        @Override
        public String toString() {
            return String.format("Transaction %s in state %s", transactionId, state);
        }
    }

    private static final class UpdateManager {
        private final TimeSpan tableCacheTime;
        private Object updateSource;
        private Date updateTimeStamp;
        private final Stopwatch timer;

        private UpdateManager(final TimeSpan initial){
            tableCacheTime = initial;
            timer = Stopwatch.createUnstarted();
        }

        private boolean stop() {
            if (timer.isRunning()) {
                timer.stop();
                return true;
            }
            else return false;
        }

        private boolean start(){
            if(timer.isRunning())
                return false;
            else {
                timer.start();
                return true;
            }
        }

        private boolean isEmpty(){
            final long elapsed = timer.elapsed(tableCacheTime.unit);
            return elapsed > tableCacheTime.duration;
        }

        private UpdateManager(){
            this(new TimeSpan(5, TimeUnit.SECONDS));
        }

        private void reset(){
            timer.reset();
        }

        private void updateCompleted(final Object source){
            this.updateSource = source;
            this.updateTimeStamp = new Date();
        }

        private Object getUpdateSource(){
            return updateSource;
        }

        private Date getUpdateTimeStamp(){
            return updateTimeStamp;
        }
    }

    private final AttributeAccessor _connector;
    private static final String TABLE_CACHE_TIME_PARAM = "tableCacheTime";
    private static final String USE_ROW_STATUS_PARAM = "useRowStatus";
    private final UpdateManager cacheManager;

    private static MONamedColumn[] createColumns(final ArrayType<?> tableType,
                                                           final MOAccess access,
                                                           final boolean useRowStatus){
        int columnID = 2;
        final List<MONamedColumn> columns = Lists.newArrayListWithCapacity(2);
        //column with array values
        columns.add(new MONamedColumn(columnID++, tableType, access));
        //add RowStatus column
        if(useRowStatus)
            columns.add(new MORowStatusColumn(columnID));
        return ArrayUtils.toArray(columns, MONamedColumn.class);
    }

    private static MONamedColumn[] createColumns(final CompositeType type,
                                                    final MOAccess access){
        int columnID = 2;
        //each key is in separated column
        final List<MONamedColumn> columns = Lists.newArrayListWithCapacity(type.keySet().size());
        for(final String itemName: type.keySet())
            columns.add(new MONamedColumn(columnID++, type, itemName, access));
        return ArrayUtils.toArray(columns, MONamedColumn.class);
    }

    private static MONamedColumn[] createColumns(final TabularType type,
                                                  final MOAccess access,
                                                  final boolean useRowStatus){
        int columnID = 2;
        //each key is in separated column

        final CompositeType rowType = type.getRowType();
        final List<MONamedColumn> columns = Lists.newArrayListWithCapacity(rowType.keySet().size() + 1);
        for(final String columnName: rowType.keySet())
            columns.add(new MONamedColumn(columnID++, type, columnName, access));
        //add RowStatus column
        if(useRowStatus)
            columns.add(new MORowStatusColumn(columnID));
        return ArrayUtils.toArray(columns, MONamedColumn.class);
    }

    private static MONamedColumn[] createColumns(final OpenType<?> type,
                                                           final MOAccess access,
                                                           final boolean useRowStatus){
        if(type instanceof ArrayType<?>)
            return createColumns((ArrayType<?>)type, access, useRowStatus);
        else if(type instanceof CompositeType)
            return createColumns((CompositeType)type, access);
        else if(type instanceof TabularType)
            return createColumns((TabularType)type, access, useRowStatus);
        else return new MONamedColumn[0];
    }

    private static OID makeRowID(final int rowIndex){
        return new OID(new int[]{rowIndex + 1});
    }

    private static boolean shouldUseRowStatus(final Descriptor options){
        return hasField(options, USE_ROW_STATUS_PARAM) &&
                Boolean.valueOf(getField(options, USE_ROW_STATUS_PARAM, String.class));
    }

    private SnmpTableObject(final OID oid,
                            final AttributeAccessor connector){
        super(oid,
                new MOTableIndex(new MOTableSubIndex[]{new MOTableSubIndex(null, SMIConstants.SYNTAX_INTEGER, 1, 1)}),
                createColumns(connector.getOpenType(), getAccessRestrictions(connector.getMetadata(), true), shouldUseRowStatus(connector.getMetadata().getDescriptor()))
        );
        //setup table model
        final DefaultMOMutableTableModel<DefaultMOMutableRow2PC> tableModel = new DefaultMOMutableTableModel<>();
        tableModel.setRowFactory(new DefaultMOMutableRow2PCFactory());
        this.setModel(tableModel);
        //save additional fields
        _connector = connector;
        cacheManager = hasField(connector.getMetadata().getDescriptor(), TABLE_CACHE_TIME_PARAM) ?
                new UpdateManager(new TimeSpan(Integer.valueOf(getField(connector.getMetadata().getDescriptor(), TABLE_CACHE_TIME_PARAM, String.class)))):
                new UpdateManager();
    }

    @SpecialUse
    SnmpTableObject(final AttributeAccessor connector){
        this(new OID(SnmpAdapterConfigurationDescriptor.getOID(connector.getMetadata())), connector);
    }

    /**
     * Returns the metadata of the underlying attribute.
     *
     * @return The metadata of the underlying attribute.
     */
    @Override
    public final MBeanAttributeInfo getMetadata() {
        return _connector.getMetadata();
    }

    /**
     * Determines whether this table is empty.
     * @return {@literal true}, if this table is empty; otherwise, {@literal false}.
     */
    public final boolean isEmpty(){
        return getModel().getRowCount() == 0;
    }

    private static Object fill(final AttributeAccessor connector,
                               final MOTable<DefaultMOMutableRow2PC, MONamedColumn, MOTableModel<DefaultMOMutableRow2PC>> table) throws JMException{
        final Object lastUpdateSource;
        final OpenType<?> ot = connector.getOpenType();
            if (ot instanceof ArrayType<?>) {
                fill(lastUpdateSource = connector.getValue(), table, connector.getMetadata());
            } else if (ot instanceof CompositeType) {
                lastUpdateSource = connector.getValue(CompositeData.class);
                fill((CompositeData) lastUpdateSource, table, connector.getMetadata());
            } else if (ot instanceof TabularType) {
                lastUpdateSource = connector.getValue(TabularData.class);
                fill((TabularData) lastUpdateSource, table, connector.getMetadata());
            } else {
                SnmpHelpers.log(Level.WARNING, "Source attribute table %s is not supported", table.getOID(), null);
                lastUpdateSource = null;
            }
        return lastUpdateSource;
    }

    private static void fill(final TabularData data,
                             final MOTable<DefaultMOMutableRow2PC, MONamedColumn, MOTableModel<DefaultMOMutableRow2PC>> table,
                             final DescriptorRead conversionOptions){
        final MutableInteger rowIndex = new MutableInteger(0);
        TabularDataUtils.forEachRow(data, new SafeConsumer<CompositeData>() {
            @Override
            public void accept(final CompositeData row){
                final List<Variable> cells = Lists.newArrayListWithExpectedSize(table.getColumnCount());
                for(int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++){
                    final MONamedColumn columnDef = table.getColumn(columnIndex);
                    if(MORowStatusColumn.isInstance(columnDef))
                        cells.add(columnIndex, TableRowStatus.ACTIVE.toManagedScalarValue());
                    else {
                        final Variable cell = columnDef.createCellValue(row.get(columnDef.name), conversionOptions);
                        cells.add(columnIndex, cell);
                    }
                }
                table.addRow(table.createRow(makeRowID(rowIndex.getAndIncrement()), ArrayUtils.toArray(cells, Variable.class)));
            }
        });
    }

    private static void fill(final CompositeData data,
                             final MOTable<DefaultMOMutableRow2PC, MONamedColumn, MOTableModel<DefaultMOMutableRow2PC>> table,
                             final DescriptorRead conversionOptions){
        final List<Variable> cells = Lists.newArrayListWithExpectedSize(table.getColumnCount());
        for(int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++){
            final MONamedColumn columnDef = table.getColumn(columnIndex);
            final Variable cell = columnDef.createCellValue(data.get(columnDef.name), conversionOptions);
            cells.add(columnIndex, cell);
        }
        table.addRow(table.createRow(makeRowID(0), ArrayUtils.toArray(cells, Variable.class)));
    }

    private static void fill(final Object array,
                             final MOTable<DefaultMOMutableRow2PC, MONamedColumn, MOTableModel<DefaultMOMutableRow2PC>> table,
                             final DescriptorRead conversionOptions){
        //for arrays we have only one column with values
        final MONamedColumn columnDef = table.getColumn(0);
        final boolean needRowStatus = SnmpHelpers.findColumnIndex(table, MORowStatusColumn.class) >= 0;
        for(int i = 0; i < Array.getLength(array); i++) {
            final List<Variable> cells = Lists.newArrayListWithExpectedSize(2);
            cells.add(0, columnDef.createCellValue(Array.get(array, i), conversionOptions));
            //handle row status column
            if(needRowStatus)
                cells.add(1, TableRowStatus.ACTIVE.toManagedScalarValue());
            table.addRow(table.createRow(makeRowID(i), ArrayUtils.toArray(cells, Variable.class)));
        }
    }

    private void fillTableIfNecessary() throws JMException {
        if (isEmpty()) try {
            cacheManager.updateCompleted(fill(_connector, this));
        }
        finally {
            cacheManager.stop();
            cacheManager.reset();
            cacheManager.start();
        }
        else {
            cacheManager.stop();
            try {
                if (cacheManager.isEmpty()) {
                    removeAll();
                    cacheManager.updateCompleted(fill(_connector, this));
                    cacheManager.reset();
                }
            } finally {
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
        catch (final JMException e) {
            SnmpHelpers.log(Level.SEVERE, "Unable to update SNMP table.", e);
        }
    }

    private Set<OID> dump(final ArrayType<?> arrayType) throws JMException {
        final Set<OID> rowsToDelete = Sets.newHashSetWithExpectedSize(model.getRowCount());
        final int rowStatusIndex = SnmpHelpers.findColumnIndex(this, MORowStatusColumn.class);
        final List<Object> array = Lists.newArrayListWithExpectedSize(model.getRowCount());
        for (int i = 0; i < array.size(); i++) {
            final MOMutableTableRow row = model.getRow(makeRowID(i));
            if (row == null) { //cancels row sending
                SnmpHelpers.log(Level.SEVERE, "Row %s is null. Sending array is cancelled", makeRowID(i), null);
                return Collections.emptySet();
            } else if (rowStatusIndex >= 0)
                switch (TableRowStatus.parse(row.getValue(rowStatusIndex))) {
                    case CREATE_AND_GO:
                        row.setValue(rowStatusIndex, TableRowStatus.ACTIVE.toManagedScalarValue());
                    case ACTIVE:
                        break;
                    case DESTROY:
                        rowsToDelete.add(row.getIndex());
                        continue;
                    default:
                        SnmpHelpers.log(Level.WARNING, "Unsupported row status %s detected at row %s in table %s. Row is ignored.", row.getValue(rowStatusIndex), row.getIndex(), getOID(), null);
                        continue;
                }
            array.add(getColumn(0).parseCellValue(row.getValue(0), getMetadata()));
        }
        _connector.setValue(SnmpHelpers.toArray(array, arrayType));
        return rowsToDelete;
    }

    private Set<OID> dump(final CompositeType type) throws JMException{
        if(model.getRowCount() > 0) {
            final Map<String, Object> items = Maps.newHashMapWithExpectedSize(getColumnCount());
            for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
                final MONamedColumn columndDef = getColumn(columnIndex);
                final MOTableRow<?> row = model.getRow(makeRowID(0));
                if (type.containsKey(columndDef.name))
                    items.put(columndDef.name, columndDef.parseCellValue(row.getValue(columnIndex), getMetadata()));
            }
            _connector.setValue(new CompositeDataSupport(type, items));
        }
        return Collections.emptySet();
    }

    private Set<OID> dump(final TabularType type) throws JMException{
        final TabularDataSupport result = new TabularDataSupport(type);
        final Set<OID> rowsToDelete = Sets.newHashSetWithExpectedSize(model.getRowCount());
        //for each for
        next_row: for(int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++){
            final Map<String, Object> items = Maps.newHashMapWithExpectedSize(getColumnCount());
            final MOMutableTableRow row = model.getRow(makeRowID(rowIndex));
            for(int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++){
                final MONamedColumn column = getColumn(columnIndex);
                if(MORowStatusColumn.isInstance(column))
                    switch (TableRowStatus.parse(row.getValue(rowIndex))){
                        case CREATE_AND_GO:
                            row.setValue(rowIndex, TableRowStatus.ACTIVE.toManagedScalarValue());
                        case ACTIVE: continue;
                        case DESTROY:
                            rowsToDelete.add(row.getIndex());
                            continue next_row;
                        default:
                            SnmpHelpers.log(Level.WARNING, "Unsupported row status %s detected at row %s in table %s. Row is ignored.", row.getValue(rowIndex), row.getIndex(), getOID(), null);
                        continue next_row;
                    }
                else  //data row
                    items.put(column.name, column.parseCellValue(row.getValue(columnIndex), getMetadata()));
            }
            result.put(new CompositeDataSupport(type.getRowType(), items));
        }
        _connector.setValue(result);
        return rowsToDelete;
    }

    //this method is synchronized because sending table rows to connector is atomic
    private synchronized void dumpTable() throws JMException {
        Set<OID> rowsToDelete = Collections.emptySet();
        final OpenType<?> attributeType = _connector.getOpenType();
        if(attributeType instanceof ArrayType<?>)
            rowsToDelete = dump((ArrayType<?>) attributeType);
        else if(attributeType instanceof CompositeType)
            rowsToDelete = dump((CompositeType) attributeType);
        else if(attributeType instanceof TabularType)
            rowsToDelete = dump((TabularType)attributeType);
        //remove rows
        for(final OID row: rowsToDelete)
            removeRow(row);
    }

    @Override
    public final void prepare(final SubRequest request) {
        try {
            TransactionInfo.pendingState(request, TransactionState.PREPARE);
        }
        catch (final Exception e){
            SnmpHelpers.log(Level.SEVERE, "Unable to prepare transaction for %s table", getOID(), e);
        }
        finally {
            super.prepare(request);
        }
    }

    @Override
    public final void commit(final SubRequest request) {
        try {
            TransactionInfo.pendingState(request, TransactionState.COMMIT);
        }
        catch (final Exception e){
            SnmpHelpers.log(Level.SEVERE, "Unable to commit %s table", getOID(), e);
        }
        finally {
            super.commit(request);
        }
    }

    @Override
    public final void undo(final SubRequest request) {
        try {
            TransactionInfo.pendingState(request, TransactionState.ROLLBACK);
        }
        catch (final Exception e){
            SnmpHelpers.log(Level.SEVERE, "Unable to undo changes in %s table", getOID(), e);
        }
        finally {
            super.undo(request);
        }
    }

    @Override
    public final void cleanup(final SubRequest request) {
        try {
            final TransactionInfo info = TransactionInfo.pendingState(request, TransactionState.CLEANUP);
            switch (info.getCompletionState()) {
                case SUCCESS:
                        dumpTable();
                    break;
                case ROLLED_BACK:
                    SnmpHelpers.log(Level.WARNING, "Transaction for table %s aborted.", getOID(), null);
                    break;
            }
        }
        catch (final Exception e){
            SnmpHelpers.log(Level.SEVERE, "Unable to clean table %s", getOID(), e);
            request.setErrorStatus(SnmpConstants.SNMP_ERROR_COMMIT_FAILED);
            request.completed();
        }
        finally {
            super.cleanup(request);
        }
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return objectType.isAssignableFrom(AttributeAccessor.class) ? objectType.cast(_connector) : null;
    }
}
