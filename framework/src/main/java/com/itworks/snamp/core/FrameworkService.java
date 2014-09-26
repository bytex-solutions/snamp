package com.itworks.snamp.core;

import com.itworks.snamp.Aggregator;
import com.itworks.snamp.internal.annotations.Internal;
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
}
