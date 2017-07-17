package com.bytex.snamp.configuration;

import com.bytex.snamp.AbstractAggregator;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class ConfigurationEntityDescriptionProviderImpl extends AbstractAggregator implements ConfigurationEntityDescriptionProvider {
    private final Collection<ConfigurationEntityDescription> descriptions;

    /**
     * Initializes a new instance of the configuration description provider.
     * @param descriptions A set of available descriptors.
     */
    public ConfigurationEntityDescriptionProviderImpl(final ConfigurationEntityDescription<?>... descriptions){
        this.descriptions = ImmutableList.copyOf(descriptions);
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
        if (configurationEntity == null) throw new NullPointerException("configurationEntity is null.");
        return descriptions.stream()
                .filter(description -> configurationEntity.equals(description.getEntityType()))
                .findFirst()
                .orElse(null);
    }
}
