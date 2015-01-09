package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.core.OsgiLoggingContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxAdapterHelpers {
    static final String ADAPTER_NAME = "jmx";
    static final String JMX_ENTITY_OPTION = "jmx-compliant";
    private static final String LOGGER_NAME = AbstractResourceAdapter.getLoggerName(ADAPTER_NAME);

    private JmxAdapterHelpers(){

    }

    static boolean isJmxCompliantAttribute(final AttributeMetadata attr){
        return attr.containsKey(JMX_ENTITY_OPTION) && Boolean.valueOf(attr.get(JMX_ENTITY_OPTION));
    }

    static <E extends Exception> void withLogger(final Consumer<Logger, E> contextBody) throws E {
        OsgiLoggingContext.within(LOGGER_NAME, contextBody);
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