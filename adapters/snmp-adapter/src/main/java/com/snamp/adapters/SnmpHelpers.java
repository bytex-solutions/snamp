package com.snamp.adapters;

import com.snamp.connectors.AttributeMetadata;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOAccessImpl;

/**
 * @author roman
 */
final class SnmpHelpers {
    private SnmpHelpers(){

    }

    public static MOAccess getAccessRestrictions(final AttributeMetadata metadata){
        switch ((metadata.canWrite() ? 1 : 0) << 1 | (metadata.canRead() ? 1 : 0)){
            //case 0: case 1:
            default: return MOAccessImpl.ACCESS_READ_ONLY;
            case 2: return MOAccessImpl.ACCESS_WRITE_ONLY;
            case 3: return MOAccessImpl.ACCESS_READ_WRITE;
        }
    }
}
