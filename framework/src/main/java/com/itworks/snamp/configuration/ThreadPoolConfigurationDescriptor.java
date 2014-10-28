package com.itworks.snamp.configuration;

import static com.itworks.snamp.configuration.AgentConfiguration.ConfigurationEntity;

/**
 * Represents configuration descriptor for the configurable entity that has its own {@link java.util.concurrent.ExecutorService}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ThreadPoolConfigurationDescriptor<T extends ConfigurationEntity> extends ConfigurationEntityDescription<T> {
    /**
     * Configuration property for the min pool size.
     */
    final String MIN_POOL_SIZE_PROPERTY = "minPoolSize";
    /**
     * Configuration property for the max pool size.
     */
    final String MAX_POOL_SIZE_PROPERTY = "maxPoolSize";
    /**
     * Configuration property for the queue size.
     */
    final String QUEUE_SIZE_PROPERTY = "queueSize";
    /**
     * Configuration property for the keep alive time.
     */
    final String KEEP_ALIVE_TIME_PROPERTY = "keepAliveTime";
    /**
     * Configuration property for the priority.
     */
    final String PRIORITY_PROPERTY = "priority";
}
