package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.connectors.AbstractManagedResourceConnector;

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
}
