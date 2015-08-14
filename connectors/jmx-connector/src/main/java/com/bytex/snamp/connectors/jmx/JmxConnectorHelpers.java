package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.core.LoggingScope;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import java.util.logging.Level;

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

    private static BundleContext getBundleContext(){
        return FrameworkUtil.getBundle(JmxConnectorHelpers.class).getBundleContext();
    }

    private static void log(final Level lvl, final String message, final Object[] args, final Throwable e){
        try(final LoggingScope logger = new LoggingScope(LOGGER_NAME, getBundleContext())){
            logger.log(lvl, String.format(message, args), e);
        }
    }

    static void log(final Level lvl, final String message){
        log(lvl, message, new Object[0], null);
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
