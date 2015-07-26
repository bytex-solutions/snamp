package com.bytex.snamp.core;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.internal.annotations.Internal;
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
