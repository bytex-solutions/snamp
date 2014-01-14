package com.snamp.adapters;

import com.snamp.connectors.AttributeMetadata;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.Variable;

/**
 * @author Roman Sakno
 */
final class SnmpHelpers {
    private SnmpHelpers(){

    }

    public static MOAccess getAccessRestrictions(final AttributeMetadata metadata, final boolean mayCreate){
        switch ((metadata.canWrite() ? 1 : 0) << 1 | (metadata.canRead() ? 1 : 0)){
            //case 0: case 1:
            default: return MOAccessImpl.ACCESS_READ_ONLY;
            case 2: return mayCreate ? new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_WRITE | MOAccessImpl.ACCESSIBLE_FOR_CREATE) : MOAccessImpl.ACCESS_WRITE_ONLY;
            case 3: return mayCreate ? new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE | MOAccessImpl.ACCESSIBLE_FOR_CREATE) :  MOAccessImpl.ACCESS_READ_WRITE;
        }
    }

    public static MOAccess getAccessRestrictions(final AttributeMetadata metadata){
        return getAccessRestrictions(metadata, false);
    }

    public static <COLUMN extends MOColumn<? extends Variable>> COLUMN findColumn(final MOTable<?, ? extends MOColumn<? extends Variable>, ?> table, final Class<COLUMN> columnType){
        for(final MOColumn<? extends Variable> column: table.getColumns())
            if(columnType.isInstance(column)) return columnType.cast(column);
        return null;
    }

    public static int findColumnIndex(final MOTable<?, ? extends MOColumn<? extends Variable>, ?> table, final Class<? extends MOColumn<? extends Variable>> columnType){
        final MOColumn<? extends Variable> column = findColumn(table, columnType);
        return column != null ? column.getColumnID() : -1;
    }
}
