package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import org.snmp4j.smi.OID;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpConnectorHelpers {
    public static final String CONNECTOR_NAME = "snmp";
    private static final Logger logger = AbstractManagedResourceConnector.getLogger(CONNECTOR_NAME);

    private SnmpConnectorHelpers(){

    }

    public static Logger getLogger(){
        return logger;
    }

    private static int[] getPostfix(final int[] prefix, final int[] full){
        return full.length > prefix.length ?
                Arrays.copyOfRange(full, prefix.length, full.length):
                new int[0];
    }

    public static OID getPostfix(final OID prefix, final OID oid){
        return oid.startsWith(prefix) ? new OID(getPostfix(prefix.getValue(), oid.getValue())) : new OID();
    }
}
