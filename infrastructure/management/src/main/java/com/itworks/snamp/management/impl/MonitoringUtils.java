package com.itworks.snamp.management.impl;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.core.OSGiLoggingContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Monitoring utils.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MonitoringUtils {
    private static final String LOGGER_NAME = "com.itworks.snamp.management.impl";

    private MonitoringUtils(){

    }

    /**
     * With logger.
     *
     * @param <E>  the type parameter
     * @param loggerHandler the logger handler
     * @throws E the e
     */
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

    /**
     * Log void.
     *
     * @param lvl the lvl
     * @param message the message
     * @param e the e
     */
    static void log(final Level lvl, final String message, final Throwable e){
        log(lvl, message, new Object[0], e);
    }

    /**
     * Gets logger.
     *
     * @return the logger
     */
    static Logger getLogger() {
        return Logger.getLogger(LOGGER_NAME);
    }

    /**
     * Interface static initialize.
     *
     * @param <T>  the type parameter
     * @param initializer the initializer
     * @return the t
     * @throws ExceptionInInitializerError the exception in initializer error
     */
    static <T> T interfaceStaticInitialize(final ExceptionalCallable<T, ?> initializer) throws ExceptionInInitializerError{
        try {
            return initializer.call();
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
