package com.bytex.snamp.configuration;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityConfiguration;

/**
 * Represents configuration descriptor for the configurable entity that has its own {@link java.util.concurrent.ExecutorService}.
 * @author Roman Sakno
 * @version 1.2.0
 * @since 1.0
 * @deprecated Since 1.2.0 SNAMP uses centralized configuration of thread pools used by SNAMP components. See {@link com.bytex.snamp.concurrent.ThreadPoolRepository}. This class will be removed in the next version.
 */
@Deprecated
public interface ThreadPoolConfigurationDescriptor<T extends EntityConfiguration> extends ConfigurationEntityDescription<T> {
    /**
     * Configuration property for the min pool size.
     */
    String MIN_POOL_SIZE_PROPERTY = "minPoolSize";
    /**
     * Configuration property for the max pool size.
     */
    String MAX_POOL_SIZE_PROPERTY = "maxPoolSize";
    /**
     * Configuration property for the queue size.
     */
    String QUEUE_SIZE_PROPERTY = "queueSize";
    /**
     * Configuration property for the keep alive time.
     */
    String KEEP_ALIVE_TIME_PROPERTY = "keepAliveTime";
    /**
     * Configuration property for the priority.
     */
    String PRIORITY_PROPERTY = "priority";
}
