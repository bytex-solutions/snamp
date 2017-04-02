package com.bytex.snamp.configuration;

import javax.annotation.Nonnull;

/**
 * Represents template of managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagedResourceTemplate extends TypedEntityConfiguration, ThreadPoolConfigurationSupport {
    /**
     * Represents name of configuration parameter that can be used to enable Smart mode of the connector.
     */
    String SMART_MODE_KEY = "smartMode";

    @Nonnull
    EntityMap<? extends AttributeConfiguration> getAttributes();

    @Nonnull
    EntityMap<? extends EventConfiguration> getEvents();

    @Nonnull
    EntityMap<? extends OperationConfiguration> getOperations();
}
