package com.snamp.adapters;

import com.snamp.connectors.ManagementEntityTabularType;
import com.snamp.connectors.ManagementEntityType;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOMutableColumn;
import org.snmp4j.smi.Variable;

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

    protected MONamedColumn(final int columnID, final String columnName, final int columnType, final MOAccess access, final boolean isIndexed){
        super(columnID, columnType, access);
        this.name = columnName;
        this.isIndexed = isIndexed;
    }

    protected MONamedColumn(final int columnID, final String columnName, final ManagementEntityType columnType, final MOAccess access, final boolean isIndexed){
        this(columnID, columnName, SnmpType.getSyntax(columnType), access, isIndexed);
    }

    public MONamedColumn(final int columnID, final String columnName, final ManagementEntityTabularType type, final MOAccess access) {
        this(columnID, columnName, type.getColumnType(columnName), access, type.isIndexed(columnName));
    }

    /**
     * Determines whether this column is synthetic and doesn't contain any payload.
     * @return {@literal true}, if this column is synthetic and doesn't contain any payload; otherwise, {@literal false}.
     */
    public boolean isSynthetic(){
        return false;
    }
}
