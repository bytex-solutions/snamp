package com.snamp.adapters;

import com.snamp.connectors.AttributeMetadata;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOAccessImpl;

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
}
