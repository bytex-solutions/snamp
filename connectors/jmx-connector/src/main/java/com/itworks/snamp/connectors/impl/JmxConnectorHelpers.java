package com.itworks.snamp.connectors.impl;

import com.itworks.snamp.connectors.AbstractManagementConnector;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxConnectorHelpers {
    /**
     * Represents name of the management connector.
     */
    public static final String CONNECTOR_NAME = "jmx";

    private JmxConnectorHelpers(){

    }

    /**
     * Gets logger associated with JMX management connector.
     * @return
     */
    public static Logger getLogger(){
        return AbstractManagementConnector.getLogger(CONNECTOR_NAME);
    }
}
