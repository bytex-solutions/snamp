package com.itworks.snamp.connectors.rshell;

import com.itworks.snamp.connectors.AbstractManagedResourceConnector;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellConnectorHelpers {
    private RShellConnectorHelpers(){

    }

    static String CONNECTOR_NAME = "rshell";

    static Logger getLogger(){
        return AbstractManagedResourceConnector.getLogger(CONNECTOR_NAME);
    }
}
