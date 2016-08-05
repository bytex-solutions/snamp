package com.bytex.snamp.configuration;

/**
 * Represents hosting configuration (front-end configuration).
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public interface ResourceAdapterConfiguration extends EntityConfiguration {
    /**
     * Represents name of configuration parameter that points to thread pool in {@link com.bytex.snamp.concurrent.ThreadPoolRepository}
     * service used by adapter.
     * @since 1.2
     */
    String THREAD_POOL_KEY = "threadPool";

    /**
     * Gets the hosting adapter name.
     * @return The hosting adapter name.
     */
    String getAdapterName();

    /**
     * Sets the hosting adapter name.
     * @param adapterName The adapter name.
     */
    void setAdapterName(final String adapterName);
}
