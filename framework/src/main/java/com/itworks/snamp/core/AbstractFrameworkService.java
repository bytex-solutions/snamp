package com.itworks.snamp.core;

import com.itworks.snamp.AbstractAggregator;
import org.apache.commons.collections4.Factory;
import com.itworks.snamp.internal.Internal;

import java.util.logging.*;

/**
 * Represents an abstract class for building SNAMP platform services.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public abstract class AbstractFrameworkService extends AbstractAggregator implements FrameworkService {
    private final Logger logger;

    /**
     * Initializes a new instance of the platform service.
     * @param loggerInstance A logger associated with this instance of the platform service.
     */
    protected AbstractFrameworkService(final Logger loggerInstance){
        logger = loggerInstance != null ? loggerInstance : Logger.getLogger(getClass().getName());
    }

    /**
     * Initializes a new instance of the platform service.
     * @param loggerName The name of the logger to be associated with this instance of the platform service.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected AbstractFrameworkService(final String loggerName){
        this(Logger.getLogger(loggerName));
    }

    /**
     * Throws an exception in the caller thread and logs it to the {@link Logger}.
     * @param logLevel The exception logging level.
     * @param e An exception to throw.
     * @param <E> Type of the exception to throw.
     * @throws E An exception to be thrown by this method.
     */
    protected final <E extends Throwable> void throwAndLog(final Level logLevel, final E e) throws E{
        logger.log(logLevel, e.getLocalizedMessage(), e);
        throw e;
    }

    /**
     * Throws an exception in the caller thread and logs it to the {@link Logger}.
     * @param logLevel The exception logging level.
     * @param e An exception to throw.
     * @param <E> Type of the exception to throw.
     * @throws E An exception to be thrown by this method.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected final <E extends Throwable> void throwAndLog(final Level logLevel, final Factory<E> e) throws E{
        throwAndLog(logLevel, e.create());
    }

    /**
     * Gets a logger associated with this platform service.
     * @return A logger associated with this platform service.
     */
    @Aggregation
    public final Logger getLogger() {
        return logger;
    }
}
