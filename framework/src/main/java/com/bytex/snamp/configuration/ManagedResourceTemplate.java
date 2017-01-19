package com.bytex.snamp.configuration;

import java.util.Map;

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

    @Override
    ManagedResourceTemplate asReadOnly();

    static void copyAttributes(final Map<String, ? extends AttributeConfiguration> input,
                               final EntityMap<? extends AttributeConfiguration> output){
        ConfigurationEntityCopier.copy(input, output, AttributeConfiguration::copy);
    }

    static void copyEvents(final Map<String, ? extends EventConfiguration> input,
                                   final EntityMap<? extends EventConfiguration> output) {
        ConfigurationEntityCopier.copy(input, output, EventConfiguration::copy);
    }

    static void copyOperations(final Map<String, ? extends OperationConfiguration> input,
                                       final EntityMap<? extends OperationConfiguration> output) {
        ConfigurationEntityCopier.copy(input, output, OperationConfiguration::copy);
    }

    static void copy(final ManagedResourceTemplate input, final ManagedResourceTemplate output){
        output.setType(input.getType());
        output.load(input);
        copyAttributes(input.getFeatures(AttributeConfiguration.class),
                output.getFeatures(AttributeConfiguration.class)
        );
        copyEvents(input.getFeatures(EventConfiguration.class),
                output.getFeatures(EventConfiguration.class)
        );
        copyOperations(input.getFeatures(OperationConfiguration.class),
                output.getFeatures(OperationConfiguration.class)
        );
    }
}
