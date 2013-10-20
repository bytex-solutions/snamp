package com.snamp.adapters.snmp;

import com.snamp.*;
import com.snamp.connectors.*;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.request.SubRequest;
import org.snmp4j.smi.*;
import static com.snamp.adapters.snmp.SnmpHelpers.getAccessRestrictions;

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Represents SNMP table.
 * @author roman
 */
final class SnmpTableObject implements SnmpAttributeMapping{

    /**
     * Represents named column value.
     * @param <V>
     */
    private static final class MONamedColumn<V extends Variable> extends MOMutableColumn<V>{
        private final String _columnName;

        public MONamedColumn(final int columnID, final String columnName, final AttributeTabularType type, final MOAccess access) {
            super(columnID, SnmpType.getSyntax(type.getColumnType(columnName)), access);
            this._columnName = columnName;
        }

        public final String getColumnName(){
            return _columnName;
        }
    }

    private final Reference<ManagementConnector> _connector;
    private final AttributeTabularType _tabularType;
    private final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table;
    private final TimeSpan readWriteTimeout;
    //can add or remove rows from the table
    private final boolean fixedRowStructure;

    private static MONamedColumn<Variable>[] createColumns(final AttributeTabularType tableType, final MOAccess access){
        final List<MONamedColumn<Variable>> columns = new ArrayList<>(tableType.getColumns().size());
        int columnID = 0;
        for(final String columnName: tableType.getColumns())
            columns.add(new MONamedColumn<>(columnID++, columnName, tableType, access));
        return columns.toArray(new MONamedColumn[0]);
    }

    private static MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> createEmptyTable(final OID tableId,
                                                                                                                          final AttributeTabularType tabularType,
                                                                                                                          final MOAccess access){

        return new DefaultMOTable<>(tableId,
                new MOTableIndex(new MOTableSubIndex[0]),
                createColumns(tabularType, access)
        );
    }

    private SnmpTableObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts, final AttributeMetadata attribute){
        _connector = new WeakReference<>(connector);
        _tabularType = (AttributeTabularType)attribute.getAttributeType();
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
    }

    /**
     * Initializes a new SNMP table wrapper for the specified attribute.
     * @param oid
     * @param connector
     * @param timeouts
     */
    public SnmpTableObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        this(oid, connector, timeouts, connector.getAttributeInfo(oid));
    }

    private static AttributeMetadata getMetadata(final ManagementConnector connector, final OID attributeId){
        return connector != null ? connector.getAttributeInfo(attributeId.toString()) : null;
    }

    /**
     * Returns the metadata of the underlying attribute.
     *
     * @return The metadata of the underlying attribute.
     */
    @Override
    public AttributeMetadata getMetadata() {
        return getMetadata(_connector.get(), table.getOID());
    }

    private static boolean isEmpty(final MOTable<? extends MOTableRow, ? extends MOColumn, ? extends MOTableModel> table){
        return table.getModel().getRowCount() == 0;
    }

    private static Variable convert(final Object cellValue, final AttributeTypeInfo cellType){
        final SnmpType typeProvider = SnmpType.map(cellType);
        return typeProvider.convert(cellValue, cellType);
    }

    private static Object convert(final Variable cellValue, final AttributeTypeInfo cellType){
        final SnmpType typeProvider = SnmpType.map(cellType);
        return typeProvider.convert(cellValue, cellType);
    }

    private static Variable[] createRow(final int rowIndex, final Table<String> values, final MONamedColumn<Variable>[] columns, final AttributeTabularType type){
        final Variable[] result = new Variable[columns.length];
        for(int columnIndex = 0; columnIndex < result.length; columnIndex++){
            final MONamedColumn<Variable> columnDef = columns[columnIndex];
            final Variable cellValue = convert(values.getCell(columnDef.getColumnName(), rowIndex), type.getColumnType(columnDef.getColumnName()));
            result[columnIndex] = cellValue;
        }
        return result;
    }

    private static void fill(final Table<String> values, final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table, final AttributeTabularType type){
        //create rows
        for(int rowIndex = 0; rowIndex < values.getRowCount(); rowIndex++){
            final OID rowID = new OID(String.format("%s.%s", table.getOID(), rowIndex));
            final MOTableRow<Variable> newRow = table.createRow(rowID, createRow(rowIndex, values, table.getColumns(), type));
            table.addRow(newRow);
        }
    }

    private static void fill(final ManagementConnector connector, final MOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> table, final AttributeTabularType type, final TimeSpan rwTimeout) throws TimeoutException {
        if(type.canConvertTo(Table.class))
            fill(type.convertTo(connector.getAttribute(table.getOID().toString(), rwTimeout, new SimpleTable<String>()), Table.class), table, type);

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
    public Variable getValue(final OID instanceOID) {
        if(isEmpty(table))
            try {
                fill(_connector.get(), table, _tabularType, readWriteTimeout);
            } catch (final TimeoutException e) {
                log.warning(e.getLocalizedMessage());
                return new Null(SMIConstants.EXCEPTION_NO_SUCH_INSTANCE);
            }
        return table.getValue(instanceOID);
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
    public boolean setValue(final VariableBinding newValueAndInstanceOID) {
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
        return table.find(range);
    }

    /**
     * Processes a GET request and return the result in the supplied sub-request.
     *
     * @param request the <code>SubRequest</code> to process.
     */
    @Override
    public void get(final SubRequest request) {
        if(isEmpty(table))
            try {
                fill(_connector.get(), table, _tabularType, readWriteTimeout);
            }
            catch (final TimeoutException e) {
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
    public boolean next(final SubRequest request) {
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
    public void prepare(final SubRequest request) {
       table.prepare(request);
    }

    /**
     * Commits a previously prepared SET (sub)request. This is the second phase
     * of a two phase commit. The change is committed but the resources locked
     * during prepare not freed yet.
     *
     * @param request the <code>SubRequest</code> to process.
     */
    @Override
    public void commit(final SubRequest request) {
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
    public void undo(final SubRequest request) {
        table.undo(request);
    }

    /**
     * Cleansup a (sub)request and frees all resources locked during
     * the preparation phase.
     *
     * @param request the <code>SubRequest</code> to process.
     */
    @Override
    public void cleanup(final SubRequest request) {
        table.cleanup(request);
    }
}
