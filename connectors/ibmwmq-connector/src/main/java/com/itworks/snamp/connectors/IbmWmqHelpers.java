package com.itworks.snamp.connectors;

import com.itworks.snamp.connectors.AbstractManagementConnectorFactory;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class IbmWmqHelpers {
    public static final String CONNECTOR_NAME = "ibm-wmq";

    public static Logger getLogger() {
        return AbstractManagementConnectorFactory.getLogger(CONNECTOR_NAME);
    }
}
