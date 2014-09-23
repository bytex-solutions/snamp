package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOMutableColumn;
import org.snmp4j.smi.Variable;

import java.util.Map;

/**
 * Represents named column value.
 * @param <V>
 */
class MONamedColumn<V extends Variable> extends MOMutableColumn<V> {
    /**
     * Represents the name of the column.
     */
    public final String name;
    /**
     * Determines whether this column is indexed.
     */
    public final boolean isIndexed;

    /**
     * Represents column type.
     */
    protected final SnmpType columnType;

    protected MONamedColumn(final int columnID,
                            final String columnName,
                            final SnmpType columnType,
                            final MOAccess access, final boolean isIndexed){
        super(columnID, columnType.getSyntax(), access);
        this.name = columnName;
        this.isIndexed = isIndexed;
        this.columnType = columnType;
    }

    protected MONamedColumn(final int columnID, final String columnName, final ManagedEntityType columnType, final MOAccess access, final boolean isIndexed){
        this(columnID, columnName, SnmpType.map(columnType), access, isIndexed);
    }

    public MONamedColumn(final int columnID, final String columnName, final ManagedEntityTabularType type, final MOAccess access) {
        this(columnID, columnName, type.getColumnType(columnName), access, type.isIndexed(columnName));
    }

    /**
     * Determines whether this column is synthetic and doesn't contain any payload.
     * @return {@literal true}, if this column is synthetic and doesn't contain any payload; otherwise, {@literal false}.
     */
    public boolean isSynthetic(){
        return false;
    }

    public Object parseCellValue(final V value, final ManagedEntityType ct, final Map<String, String> conversionOptions) {
        return columnType.convert(value, ct, conversionOptions);
    }

    @SuppressWarnings("unchecked")
    public V createCellValue(final Object cell, final ManagedEntityType ct, final Map<String, String> conversionOptions) {
        return (V)columnType.convert(cell, ct, conversionOptions);
    }
}
