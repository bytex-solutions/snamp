package com.bytex.snamp.configuration;

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

    /**
     * Gets a collection of configured manageable elements for this target.
     * @param featureType The type of the manageable element.
     * @param <T> The type of the manageable element.
     * @return A map of manageable elements; or {@literal null}, if element type is not supported.
     * @see AttributeConfiguration
     * @see EventConfiguration
     * @see OperationConfiguration
     */
    <T extends FeatureConfiguration> EntityMap<? extends T> getFeatures(final Class<T> featureType);

}
