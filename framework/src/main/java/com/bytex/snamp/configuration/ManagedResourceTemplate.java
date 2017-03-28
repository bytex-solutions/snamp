package com.bytex.snamp.configuration;

import javax.annotation.Nonnull;

/**
 * Represents template of managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagedResourceTemplate extends TypedEntityConfiguration {

    /**
     * Represents name of configuration parameter that can be used to enable Smart mode of the connector.
     */
    String SMART_MODE_KEY = "smartMode";

    /**
     * Represents name of configuration parameter that points to thread pool in {@link com.bytex.snamp.concurrent.ThreadPoolRepository}
     * service used by connector.
     * @since 1.2
     */
    String THREAD_POOL_KEY = "threadPool";

    @Nonnull
    EntityMap<? extends AttributeConfiguration> getAttributes();

    @Nonnull
    EntityMap<? extends EventConfiguration> getEvents();

    @Nonnull
    EntityMap<? extends OperationConfiguration> getOperations();
}
