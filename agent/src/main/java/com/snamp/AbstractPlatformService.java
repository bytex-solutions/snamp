package com.snamp;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an abstract class for building SNAMP platform services.
 * @author roman
 */
public abstract class AbstractPlatformService extends AbstractAggregated implements PlatformService {
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

    protected final <E extends Throwable> void throwAndLog(final Level logLevel, final E e) throws E{
        logger.log(logLevel, e.getLocalizedMessage(), e);
        throw e;
    }

    protected final <E extends Throwable> void throwAndLog(final Level logLevel, final Activator<E> e) throws E{
        throwAndLog(logLevel, e.newInstance());
    }

    /**
     * Gets a logger associated with this platform service.
     * @return
     */
    @Override
    @Aggregation
    public final Logger getLogger() {
        return logger;
    }
}
