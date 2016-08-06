package com.bytex.snamp.configuration;

import com.bytex.snamp.AbstractAggregator;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

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
        if (configurationEntity == null) throw new NullPointerException("configurationEntity is null.");
        return descriptions.stream()
                .filter(description -> configurationEntity.equals(description.getEntityType()))
                .findFirst()
                .orElseGet(() -> null);
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
