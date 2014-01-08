package com.snamp.adapters;

import com.snamp.connectors.*;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.*;
import static com.snamp.adapters.SnmpHelpers.getAccessRestrictions;
import static com.snamp.connectors.util.ManagementEntityTypeHelper.*;

import com.snamp.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Represents SNMP table.
 * @author Roman Sakno
 */
final class SnmpTableObject extends DefaultMOTable<MOTableRow<Variable>, MONamedColumn<Variable>, MOTableModel<MOTableRow<Variable>>> implements SnmpAttributeMapping, UpdatableManagedObject{


    private static final class UpdateManager extends CountdownTimer{
        public final TimeSpan tableCacheTime;
        private Object updateSource;
        private Date updateTimeStamp;

        public UpdateManager(final TimeSpan initial){
            super(initial);
            this.tableCacheTime = initial;
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
    private final UpdateManager cacheManager;
    private Map<String, String> conversionOptions;

    private static MONamedColumn<Variable>[] createColumns(final ManagementEntityTabularType tableType, final MOAccess access){
        int columnID = 2;
        if(WellKnownTypeSystem.isArray(tableType)) //hides column with array indexes
            return new MONamedColumn[]{new MONamedColumn<>(columnID, ManagementEntityTypeBuilder.AbstractManagementEntityArrayType.VALUE_COLUMN_NAME, tableType, access)};

        else {
            final List<MONamedColumn<Variable>> columns = new ArrayList<>(tableType.getColumns().size());
            for(final String columnName: tableType.getColumns())
                columns.add(new MONamedColumn<>(columnID++, columnName, tableType, access));
            return columns.toArray(new MONamedColumn[columns.size()]);
        }
    }

    private static OID makeRowID(final int rowIndex){
        return new OID(new int[]{rowIndex+1});
    }

    private SnmpTableObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts, final AttributeMetadata attribute){
        super(new OID(oid),
                new MOTableIndex(new MOTableSubIndex[]{new MOTableSubIndex(null, SMIConstants.SYNTAX_INTEGER, 1, 1)}),
                createColumns((ManagementEntityTabularType)attribute.getType(), getAccessRestrictions(attribute))
        );
        //setup table model
        final DefaultMOMutableTableModel<MOTableRow<Variable>> tableModel = new DefaultMOMutableTableModel<>();
        tableModel.setRowFactory(new DefaultMOMutableRow2PCFactory());
        this.setModel(tableModel);
        //save additional fields
        _connector = connector;
        readWriteTimeout = timeouts;
        attributeInfo = attribute;
        //default table refresh is 5 seconds
        cacheManager = new UpdateManager( attribute.containsKey(TABLE_CACHE_TIME_PARAM) ?
                new TimeSpan(Integer.valueOf(attribute.get(TABLE_CACHE_TIME_PARAM))):
                new TimeSpan(5, TimeUnit.SECONDS));
        conversionOptions = Collections.emptyMap();
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
            final Variable cellValue = convert(values.getCell(columnDef.columnName, rowIndex), type.getColumnType(columnDef.columnName), conversionOptions);
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

    @Override
    public final void setAttributeOptions(Map<String, String> options) {
        this.conversionOptions = options;
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
}
