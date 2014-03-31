package com.snamp.configuration;

import static com.snamp.configuration.AgentConfiguration.ConfigurationEntity;

/**
 * Represents configuration description provider.
 * <p>
 *     An implementation of this interface can be retrieved from SNAMP plugin
 *     using {@link com.snamp.core.PlatformPlugin#queryObject(Class)} method.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ConfigurationEntityDescriptionProvider {

    /**
     * Retrieves configuration description for the specified configuration element type.
     * @param configurationEntity Type of the configuration element.
     * @param <T> Type of the configuration element.
     * @return The description of the configuration element; or {@literal null}, if description is not available.
     */
    public <T extends ConfigurationEntity> ConfigurationEntityDescription<T> getDescription(final Class<T> configurationEntity);
}
