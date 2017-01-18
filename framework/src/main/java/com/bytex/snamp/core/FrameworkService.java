package com.bytex.snamp.core;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.Internal;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents a root interface for all SNAMP framework services.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Internal
public interface FrameworkService extends Aggregator {
    /**
     * Represents category of the service.
     */
    String CATEGORY_PROPERTY = "category";

    /**
     * Gets runtime configuration of this service.
     * @return Runtime configuration of this service.
     * @implSpec Returning map is always immutable.
     */
    @Nonnull
    default Map<String, ?> getRuntimeConfiguration(){
        return ImmutableMap.of();
    }

    /**
     * Updates runtime configuration of this service.
     * @param configuration A new runtime configuration of this service.
     * @throws Exception Unable to update configuration.
     */
    default void updateConfiguration(final Map<String, ?> configuration) throws Exception{

    }
}
