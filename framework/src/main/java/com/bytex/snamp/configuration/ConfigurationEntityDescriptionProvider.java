package com.bytex.snamp.configuration;

import com.bytex.snamp.core.SupportService;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityConfiguration;

/**
 * Represents configuration description provider.
 * <p>
 *     This interface should be supplied from the SNAMP component bundle as a OSGi service.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ConfigurationEntityDescriptionProvider extends SupportService {

    /**
     * Retrieves configuration description for the specified configuration element type.
     * @param <T> Type of the configuration entity, such as {@link com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration}.
     * @param configurationEntity Type of the configuration element.
     * @return The description of the configuration element; or {@literal null}, if description is not available.
     */
    <T extends EntityConfiguration> ConfigurationEntityDescription<T> getDescription(final Class<T> configurationEntity);
}