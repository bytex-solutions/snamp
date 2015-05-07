package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.core.OSGiLoggingContext;

import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import java.util.logging.Level;
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
    static final String CONNECTOR_NAME = "jmx";
    private static final String LOGGER_NAME = AbstractManagedResourceConnector.getLoggerName(CONNECTOR_NAME);

    private JmxConnectorHelpers(){

    }

    static InvalidAttributeValueException invalidAttributeValueException(final JMException inner) {
        return new InvalidAttributeValueException(inner.getMessage()){
            @Override
            public JMException getCause() {
                return inner;
            }
        };
    }

    static <E extends Exception> void withLogger(final Consumer<Logger, E> contextBody) throws E {
        OSGiLoggingContext.within(LOGGER_NAME, contextBody);
    }

    private static void log(final Level lvl, final String message, final Object[] args, final Throwable e){
        withLogger(new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.log(lvl, String.format(message, args), e);
            }
        });
    }

    static void log(final Level lvl, final String message, final Throwable e){
        log(lvl, message, new Object[0], e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Throwable e){
        log(lvl, message, new Object[]{arg0}, e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Object arg1, final Throwable e){
        log(lvl, message, new Object[]{arg0, arg1}, e);
    }
}
