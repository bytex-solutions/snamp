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
