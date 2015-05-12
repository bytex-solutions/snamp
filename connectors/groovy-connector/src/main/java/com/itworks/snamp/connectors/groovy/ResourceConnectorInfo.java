package com.itworks.snamp.connectors.groovy;

import com.itworks.snamp.connectors.AbstractManagedResourceConnector;

import java.util.logging.Logger;

/**
 * Represents generic information about this resource connector.
 * This class cannot be inherited or instantiated.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ResourceConnectorInfo {
    /**
     * The name of the Groovy Resource Connector.
     */
    public static final String NAME = "groovy";
    private ResourceConnectorInfo(){

    }

    public static Logger getLogger(){
        return AbstractManagedResourceConnector.getLogger(NAME);
    }

    static String getLoggerName(){
        return AbstractManagedResourceConnector.getLoggerName(NAME);
    }
}
