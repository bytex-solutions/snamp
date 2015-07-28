package com.bytex.snamp.connectors.rshell;

import com.bytex.snamp.Consumer;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.core.OSGiLoggingContext;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellConnectorHelpers {
    private RShellConnectorHelpers(){

    }

    static final String CONNECTOR_NAME = "rshell";
    private static final String LOGGER_NAME = AbstractManagedResourceConnector.getLoggerName(CONNECTOR_NAME);

    static <E extends Exception> void withLogger(final Consumer<Logger, E> contextBody) throws E {
        OSGiLoggingContext.within(LOGGER_NAME, contextBody);
    }

}
