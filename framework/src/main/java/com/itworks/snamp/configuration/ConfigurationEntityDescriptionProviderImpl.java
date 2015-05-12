package com.itworks.snamp.configuration;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.configuration.AgentConfiguration.EntityConfiguration;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ConfigurationEntityDescriptionProviderImpl extends AbstractAggregator implements ConfigurationEntityDescriptionProvider {
    private final Iterable<ConfigurationEntityDescription<? extends EntityConfiguration>> descriptions;

    /**
     * Initializes a new instance of the configuration description provider.
     * @param descriptions A set of available descriptors.
     */
    @SafeVarargs
    public ConfigurationEntityDescriptionProviderImpl(final ConfigurationEntityDescription<? extends EntityConfiguration>... descriptions){
        this.descriptions = Arrays.asList(descriptions);
    }

    /**
     * Retrieves configuration description for the specified configuration element type.
     *
     * @param configurationEntity Type of the configuration element.
     * @param <T>                 Type of the configuration element.
     * @return The description of the configuration element; or {@literal null}, if description is not available.
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <T extends EntityConfiguration> ConfigurationEntityDescription<T> getDescription(final Class<T> configurationEntity) {
        for(final ConfigurationEntityDescription description: descriptions)
            if(Objects.equals(configurationEntity, description.getEntityType()))
                return description;
        return null;
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return Logger.getLogger(getClass().getName());
    }
}
