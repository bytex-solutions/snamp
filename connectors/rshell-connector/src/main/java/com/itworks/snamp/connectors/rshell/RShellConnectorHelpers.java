package com.itworks.snamp.connectors.rshell;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.core.OSGiLoggingContext;

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
    private static final String LOGGER_NAME = AbstractManagedResourceConnector.getLoggerName(CONNECTOR_NAME);

    static <E extends Exception> void withLogger(final Consumer<Logger, E> contextBody) throws E {
        OSGiLoggingContext.within(LOGGER_NAME, contextBody);
    }

}
