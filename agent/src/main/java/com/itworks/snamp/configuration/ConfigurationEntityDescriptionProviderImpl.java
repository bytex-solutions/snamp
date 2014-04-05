package com.itworks.snamp.configuration;

import java.util.*;
import com.itworks.snamp.configuration.AgentConfiguration.ConfigurationEntity;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ConfigurationEntityDescriptionProviderImpl implements ConfigurationEntityDescriptionProvider {
    private final Iterable<ConfigurationEntityDescription<? extends ConfigurationEntity>> descriptions;

    /**
     * Initializes a new instance of the configuration description provider.
     * @param descriptions A set of available descriptors.
     */
    public ConfigurationEntityDescriptionProviderImpl(final ConfigurationEntityDescription<? extends ConfigurationEntity>... descriptions){
        this.descriptions = Arrays.asList(descriptions);
    }

    /**
     * Retrieves configuration description for the specified configuration element type.
     *
     * @param configurationEntity Type of the configuration element.
     * @param <T>                 Type of the configuration element.
     * @return The description of the configuration element; or {@literal null}, if description is not available.
     */
    @Override
    public final <T extends ConfigurationEntity> ConfigurationEntityDescription<T> getDescription(final Class<T> configurationEntity) {
        for(final ConfigurationEntityDescription description: descriptions)
            if(configurationEntity.equals(description.getEntityType()))
                return description;
        return null;
    }
}
