package com.bytex.snamp.connector.discovery;

import com.bytex.snamp.configuration.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import static com.bytex.snamp.connector.discovery.DiscoveryService.DiscoveryResult;

/**
 * Represents builder of {@link DiscoveryResult} object.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class DiscoveryResultBuilder implements Supplier<DiscoveryResult> {
    private static final class DiscoveryResultImpl implements DiscoveryResult{
        private final Multimap<Class<? extends FeatureConfiguration>, FeatureConfiguration> features;

        private DiscoveryResultImpl(final Multimap<Class<? extends FeatureConfiguration>, FeatureConfiguration> features){
            this.features = HashMultimap.create(features);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends FeatureConfiguration> Collection<T> getSubResult(final Class<T> entityType) throws IllegalArgumentException {
            if(features.containsKey(entityType))
                return (Collection<T>)features.get(entityType);
            else throw new IllegalArgumentException(String.format("Entity type %s was not requested", entityType));
        }
    }

    private final Multimap<Class<? extends FeatureConfiguration>, FeatureConfiguration> features =
            HashMultimap.create();

    DiscoveryResultBuilder addFeatures(final Class<? extends FeatureConfiguration> featureType,
                                                                               final Collection<? extends FeatureConfiguration> configuration){
        features.putAll(featureType, configuration);
        return this;
    }

    public void importFeatures(final DiscoveryService service,
                               final String connectionString,
                               final Map<String, String> connectionOptions,
                               final Class<? extends FeatureConfiguration> featureType){
        addFeatures(featureType, service.discover(connectionString, connectionOptions, featureType));
    }

    public <T extends FeatureConfiguration> DiscoveryResultBuilder addFeature(final Class<T> featureType,
                                                                              final T configuration){
        features.put(featureType, configuration);
        return this;
    }

    public DiscoveryResultBuilder addAttribute(final AttributeConfiguration configuration){
        return addFeature(AttributeConfiguration.class, configuration);
    }

    public DiscoveryResultBuilder addEvent(final EventConfiguration configuration){
        return addFeature(EventConfiguration.class, configuration);
    }

    public DiscoveryResultBuilder addOperation(final OperationConfiguration configuration){
        return addFeature(OperationConfiguration.class, configuration);
    }

    /**
     * Retrieves an instance of the {@link DiscoveryResult}.
     *
     * @return an instance of the {@link DiscoveryResult}.
     */
    @Override
    public DiscoveryResult get() {
        return new DiscoveryResultImpl(features);
    }
}
