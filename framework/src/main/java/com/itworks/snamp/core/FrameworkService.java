package com.itworks.snamp.core;

import com.itworks.snamp.Aggregator;
import com.itworks.snamp.internal.Internal;

import java.util.Dictionary;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a root interface for all SNAMP framework services.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public interface FrameworkService extends Aggregator {
    /**
     * Gets logger associated with this service.
     * @return The logger associated with this service.
     */
    Logger getLogger();

    /**
     * Gets a set of properties that uniquely identifies this instance.
     * @return A set of properties that uniquely identifies this instance.
     */
    Dictionary<String, ?> getIdentity();
}
