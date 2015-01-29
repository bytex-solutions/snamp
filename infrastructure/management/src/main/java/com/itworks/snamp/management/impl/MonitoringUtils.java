package com.itworks.snamp.management.impl;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.core.OSGiLoggingContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MonitoringUtils {
    private static final String LOGGER_NAME = "com.itworks.snamp.management.impl";

    private MonitoringUtils(){

    }

    static <E extends Exception> void withLogger(final Consumer<Logger, E> loggerHandler) throws E{
        OSGiLoggingContext.within(LOGGER_NAME, loggerHandler);
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

    static Logger getLogger() {
        return Logger.getLogger(LOGGER_NAME);
    }
}
