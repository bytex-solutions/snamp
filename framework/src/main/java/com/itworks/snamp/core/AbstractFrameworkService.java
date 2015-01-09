package com.itworks.snamp.core;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.internal.annotations.Internal;

import java.util.logging.Logger;

/**
 * Represents an abstract class for building SNAMP platform services.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public abstract class AbstractFrameworkService extends AbstractAggregator implements FrameworkService {


    /**
     * Initializes a new instance of the platform service.
     */
    protected AbstractFrameworkService(){
    }

    /**
     * Gets a logger associated with this platform service.
     * @return A logger associated with this platform service.
     */
    @Aggregation
    @Override
    public abstract Logger getLogger();
}
