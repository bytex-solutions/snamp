package com.snamp;

import java.util.logging.*;

/**
 * Represents an abstract class for building SNAMP platform services.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public abstract class AbstractPlatformService extends AbstractAggregator implements PlatformService {
    private final Logger logger;

    /**
     * Initializes a new instance of the platform service.
     * @param loggerInstance A logger associated with this instance of the platform service.
     */
    protected AbstractPlatformService(final Logger loggerInstance){
        if(loggerInstance == null) throw new IllegalArgumentException("loggerInstance is null.");
        logger = loggerInstance;
    }

    /**
     * Initializes a new instance of the platform service.
     * @param loggerName The name of the logger to be associated with this instance of the platform service.
     */
    protected AbstractPlatformService(final String loggerName){
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
    protected final <E extends Throwable> void throwAndLog(final Level logLevel, final Activator<E> e) throws E{
        throwAndLog(logLevel, e.newInstance());
    }

    /**
     * Gets a logger associated with this platform service.
     * @return A logger associated with this platform service.
     */
    @Override
    @Aggregation
    public final Logger getLogger() {
        return logger;
    }
}
