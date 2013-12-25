package com.snamp.adapters;

import com.snamp.connectors.*;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.request.SubRequest;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import static com.snamp.adapters.SnmpHelpers.getAccessRestrictions;
import static com.snamp.connectors.util.ManagementEntityTypeHelper.*;

import com.snamp.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents SNMP table.
 * @author Roman Sakno
 */
final class SnmpTableObject implements SnmpAttributeMapping{
    /**
     * Represents named column value.
     * @param <V>
     */
    private static class MONamedColumn<V extends Variable> extends MOMutableColumn<V>{
        /**
         * Represents the name of the column.
         */
        public final String columnName;
        /**
         * Determines whether this column is indexed.
         */
        public final boolean isIndexed;

        protected MONamedColumn(final int columnID, final String columnName, final ManagementEntityType columnType, final MOAccess access, final boolean isIndexed){
            super(columnID, SnmpType.getSyntax(columnType), access);
            this.columnName = columnName;
            this.isIndexed = isIndexed;
        }

        public MONamedColumn(final int columnID, final String columnName, final ManagementEntityTabularType type, final MOAccess access) {
            this(columnID, columnName, type.getColumnType(columnName), access, type.isIndexed(columnName));
        }

        /**
         * Determines whether this column is syntetic and doesn't contain any payload.
         * @return
         */
        public boolean isSyntetic(){
            return false;
        }
    }

    private static final class RefreshTimer extends CountdownTimer{
        public final TimeSpan tableCacheTime;

        public RefreshTimer(final TimeSpan initial){
            super(initial);
            this.tableCacheTime = initial;
        }

        public void reset(){
            setTimerValue(tableCacheTime);
        }
    }

    private final AttributeSupport _connector;
    private final ManagementEntityTabularType _tabularType;
    private final DefaultMOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table;
    private final TimeSpan readWriteTimeout;
    //can add or remove rows from the table
    private final boolean fixedRowStructure;
    private static final String tableCacheTimeKey = "tableCacheTime";
    private final RefreshTimer tableCacheTimer;

    private static MONamedColumn<Variable>[] createColumns(final ManagementEntityTabularType tableType, final MOAccess access){
        final List<MONamedColumn<Variable>> columns = new ArrayList<>(tableType.getColumns().size());
        int columnID = 2;
        for(final String columnName: tableType.getColumns())
            columns.add(new MONamedColumn<>(columnID++, columnName, tableType, access));
        return columns.toArray(new MONamedColumn[columns.size()]);
    }

    private static DefaultMOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> createEmptyTable(final OID tableId,
                                                                                                                          final ManagementEntityTabularType tabularType,
                                                                                                                          final MOAccess access){

         final DefaultMOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table_ = new DefaultMOTable<>(tableId,
                new MOTableIndex(new MOTableSubIndex[]{new MOTableSubIndex(null, SMIConstants.SYNTAX_INTEGER, 1, 1)}),
                createColumns(tabularType, access)
        );
        DefaultMOMutableTableModel<MOTableRow<Variable>> model_ = new DefaultMOMutableTableModel<>();
        model_.setRowFactory(new DefaultMOMutableRow2PCFactory());
        table_.setModel(model_);

        return  table_;
    }

    private static OID makeRowID(final int rowIndex){
        return new OID(new int[]{rowIndex+1});
    }

    private SnmpTableObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts, final AttributeMetadata attribute){
        _connector = connector;
        _tabularType = (ManagementEntityTabularType)attribute.getType();
        //creates column structure
        table = createEmptyTable(new OID(oid), _tabularType, getAccessRestrictions(attribute));
        readWriteTimeout = timeouts;
        boolean fixedRowStructure = false;
        try{
            _tabularType.getRowCount();
            fixedRowStructure = true;
        }
        catch (final UnsupportedOperationException e){
            fixedRowStructure = false;
        }
        this.fixedRowStructure = fixedRowStructure;
        //default table refresh is 5 seconds
        tableCacheTimer = new RefreshTimer( attribute.containsKey(tableCacheTimeKey) ?
                new TimeSpan(Integer.valueOf(attribute.get(tableCacheTimeKey))):
                new TimeSpan(5, TimeUnit.SECONDS));
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

    private static AttributeMetadata getMetadata(final AttributeSupport connector, final OID attributeId){
        return connector != null ? connector.getAttributeInfo(attributeId.toString()) : null;
    }

    /**
     * Returns the metadata of the underlying attribute.
     *
     * @return The metadata of the underlying attribute.
     */
    @Override
    public AttributeMetadata getMetadata() {
        return getMetadata(_connector, table.getOID());
    }

    private static boolean isEmpty(final MOTable<? extends MOTableRow, ? extends MOColumn, ? extends MOTableModel> table){
        return table.getModel().getRowCount() == 0;
    }

    private static Variable convert(final Object cellValue, final ManagementEntityType cellType){
        final SnmpType typeProvider = SnmpType.map(cellType);
        return typeProvider.convert(cellValue, cellType);
    }

    private static Object convert(final Variable cellValue, final ManagementEntityType cellType){
        final SnmpType typeProvider = SnmpType.map(cellType);
        return typeProvider.convert(cellValue, cellType);
    }

    private static Variable[] createRow(final int rowIndex, final Table<String> values, final MONamedColumn<Variable>[] columns, final ManagementEntityTabularType type){
        final Variable[] result = new Variable[columns.length];
        for(int columnIndex = 0; columnIndex < result.length; columnIndex++){
            final MONamedColumn<Variable> columnDef = columns[columnIndex];
            final Variable cellValue = convert(values.getCell(columnDef.columnName, rowIndex), type.getColumnType(columnDef.columnName));
            result[columnIndex] = cellValue;
        }
        return result;
    }

    private static void fill(final Table<String> values, final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table, final ManagementEntityTabularType type){
        //create rows
        for(int rowIndex = 0; rowIndex < values.getRowCount(); rowIndex++){
            final OID rowID = makeRowID(rowIndex);
            final MOTableRow<Variable> newRow = table.createRow(rowID, createRow(rowIndex, values, table.getColumns(), type));
            table.addRow(newRow);
        }
    }

    private static void fill(final Object[] values, final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table, final ManagementEntityTabularType type){
        @Temporary
        final Table<String> tempTable = new SimpleTable<String>(new HashMap<String, Class<?>>(2){{
            put(ManagementEntityTypeBuilder.ManagementEntityArrayType.INDEX_COLUMN_NAME, Integer.class);
            put(ManagementEntityTypeBuilder.ManagementEntityArrayType.VALUE_COLUMN_NAME, Object.class);
        }});
        for(int arrayIndex = 0; arrayIndex < values.length; arrayIndex++){
            @Temporary
            final int firstColumnValue = arrayIndex;
            tempTable.addRow(new HashMap<String, Object>(2){{
                put(ManagementEntityTypeBuilder.ManagementEntityArrayType.INDEX_COLUMN_NAME, firstColumnValue);
                put(ManagementEntityTypeBuilder.ManagementEntityArrayType.VALUE_COLUMN_NAME, values[firstColumnValue]);
            }});
        }
        fill(tempTable, table, type);
    }

    private static void fill(final AttributeSupport connector, final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table, final ManagementEntityTabularType type, final TimeSpan rwTimeout) throws TimeoutException {
        if(supportsProjection(type, Table.class))
            fill(convertFrom(type, connector.getAttribute(table.getOID().toString(), rwTimeout, new SimpleTable<String>()), Table.class), table, type);
        else if(supportsProjection(type, Object[].class) && ManagementEntityTypeBuilder.isArray(type))
            fill(convertFrom(type, connector.getAttribute(table.getOID().toString(), rwTimeout, new Object[0]), Object[].class), table, type);
        else log.warning(String.format("Source attribute table %s is not supported", table.getOID()));
    }

    private void fillTableIfNecessary() throws TimeoutException {
        if(isEmpty(table)) try{
            fill(_connector, table, _tabularType, readWriteTimeout);
        }
        finally {
            tableCacheTimer.stop();
            tableCacheTimer.reset();
            tableCacheTimer.start();
        }
        else {
            tableCacheTimer.stop();
            try{
                if(tableCacheTimer.isEmpty()){
                    table.removeAll();
                    fill(_connector, table, _tabularType, readWriteTimeout);
                    tableCacheTimer.reset();
                }
            }
            finally{
                tableCacheTimer.start();
            }
        }
    }

    /**
     * Returns the variable (a copy thereof) with the specified instance OID
     * managed by this {@link org.snmp4j.agent.ManagedObject}.
     *
     * @param instanceOID the instance OID of the value. Thus, for scalar values with .0 suffix
     *                    and for tabular objects with table index suffix.
     * @return a copy of the requested <code>Variable</code> or <code>null</code> if
     *         such a variable does not exist.
     */
    @Override
    public synchronized Variable getValue(final OID instanceOID) {
        try {
            fillTableIfNecessary();
            return table.getValue(instanceOID);
        } catch (final TimeoutException e) {
            log.warning(e.getLocalizedMessage());
            return new Null(SMIConstants.EXCEPTION_NO_SUCH_INSTANCE);
        }
    }

    /**
     * Sets the value of a particular MIB object instance managed by
     * this {@link org.snmp4j.agent.ManagedObject}. This is a low level operation, thus
     * no change events will be fired.
     *
     * @param newValueAndInstanceOID a <code>VariableBinding</code> identifying the object instance to modify
     *                               by its OID and the new value by its variable part.
     * @return <code>true</code> if the object instance exists and has been modified
     *         successfully, <code>false</code> otherwise.
     */
    @Override
    public synchronized boolean setValue(final VariableBinding newValueAndInstanceOID) {
        return table.setValue(newValueAndInstanceOID);
    }

    /**
     * Returns the scope of object identifiers this managed object is managing.
     *
     * @return the <code>MOScope</code> that defines a range (possibly also a single
     *         or none instance OID) of object IDs managed by this managed object.
     */
    @Override
    public MOScope getScope() {
        return table.getScope();
    }

    /**
     * Finds the first object ID (OID) in the specified search range.
     *
     * @param range the <code>MOScope</code> for the search.
     * @return the <code>OID</code> that is included in the search <code>range</code>
     *         and <code>null</code> if no such instances could be found.
     */
    @Override
    public OID find(final MOScope range) {
        try {
            fillTableIfNecessary();
        } catch (final TimeoutException e) {
            log.warning(e.getLocalizedMessage());
        }
        finally {
            return table.find(range);
        }
    }

    /**
     * Processes a GET request and return the result in the supplied sub-request.
     *
     * @param request the <code>SubRequest</code> to process.
     */
    @Override
    public synchronized void get(final SubRequest request) {
        try {
            fillTableIfNecessary();
        } catch (final TimeoutException e) {
            log.warning(e.getLocalizedMessage());
            request.setErrorStatus(SMIConstants.EXCEPTION_NO_SUCH_OBJECT);
            request.completed();
            return;
        }
        table.get(request);
    }

    /**
     * Finds the successor instance for the object instance ID (OID) given
     * by the supplied sub-request and returns it within the supplied sub-request
     * object.
     *
     * @param request the <code>SubRequest</code> to process.
     * @return <code>true</code> if the search request found an appropriate instance,
     *         <code>false</code> otherwise.
     */
    @Override
    public synchronized boolean next(final SubRequest request) {
        return table.next(request);
    }

    /**
     * Prepares a SET (sub)request. This method represents the first phase of a
     * two phase commit. During preparation all necessary resources should be
     * locked in order to be able to execute the commit without claiming
     * additional resources.
     *
     * @param request the <code>SubRequest</code> to process.
     */
    @Override
    public synchronized void prepare(final SubRequest request) {
        try{
            commit(_connector, table, _tabularType, readWriteTimeout);
            table.prepare(request);
        }
        catch (final TimeoutException e){
            request.setErrorStatus(SnmpConstants.SNMP_ERROR_COMMIT_FAILED);
        }
        finally {
            tableCacheTimer.stop();
            tableCacheTimer.reset();
            tableCacheTimer.start();
        }

    }

    private static void commit(final AttributeSupport connector, final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table, final ManagementEntityTabularType type, final TimeSpan writeTimeout) throws TimeoutException {
        if(connector == null) return;
        final Map<String, Object>[] rows = new HashMap[table.getModel().getRowCount()];
        for(int rowIndex = 0; rowIndex < rows.length; rowIndex++){
            //creates a new row
            final Map<String, Object> row = rows[rowIndex] = new HashMap<>();
            //iterates through cells
            for(int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++){
                final MONamedColumn<Variable> columnDef = table.getColumn(columnIndex);
                final Variable cellValue = table.getValue(makeRowID(columnIndex));
                final ManagementEntityType columnType = type.getColumnType(columnDef.columnName);
                final SnmpType converter = SnmpType.map(columnType);
                row.put(columnDef.columnName, converter.convert(cellValue, columnType));
            }
        }
        //commits to the management connector
        if(supportsProjection(type, Map[].class))
            connector.setAttribute(table.getOID().toString(), writeTimeout, rows);
        else if(supportsProjection(type, Table.class))
            connector.setAttribute(table.getOID().toString(), writeTimeout, SimpleTable.fromArray(rows));
        else log.warning(String.format("Table %s cannot be written. The appropriate conversion type is not founded", table.getOID()));
    }

    /**
     * Commits a previously prepared SET (sub)request. This is the second phase
     * of a two phase commit. The change is committed but the resources locked
     * during prepare not freed yet.
     *
     * @param request the <code>SubRequest</code> to process.
     */
    @Override
    public synchronized void commit(final SubRequest request) {
        table.commit(request);
    }

    /**
     * Compensates (undo) a (sub)request when a commit of another subrequest
     * failed with an error. This also frees any resources locked during
     * the preparation phase.
     *
     * @param request the <code>SubRequest</code> to process.
     */
    @Override
    public synchronized void undo(final SubRequest request) {
        table.undo(request);
    }

    /**
     * Cleansup a (sub)request and frees all resources locked during
     * the preparation phase.
     *
     * @param request the <code>SubRequest</code> to process.
     */
    @Override
    public synchronized void cleanup(final SubRequest request) {
        table.cleanup(request);
    }
}
