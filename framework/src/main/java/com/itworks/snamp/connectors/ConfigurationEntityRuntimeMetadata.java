package com.itworks.snamp.connectors;

import javax.management.Descriptor;
import static com.itworks.snamp.configuration.AgentConfiguration.ConfigurationEntity;

/**
 * Represents configuration entity descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ConfigurationEntityRuntimeMetadata<E extends ConfigurationEntity> extends Descriptor {
    /**
     * The type of the configuration entity.
     * @return The type of the configuration entity.
     */
    Class<E> getEntityType();

    /**
     * Fills the specified configuration entity.
     * @param entity The configuration entity to fill.
     */
    void fill(final E entity);
}
