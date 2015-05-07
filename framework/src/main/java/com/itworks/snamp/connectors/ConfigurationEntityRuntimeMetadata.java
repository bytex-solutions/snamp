package com.itworks.snamp.connectors;

import com.itworks.snamp.jmx.CopyOnWriteDescriptor;

import javax.management.Descriptor;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.EntityConfiguration;

/**
 * Represents configuration entity descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ConfigurationEntityRuntimeMetadata<E extends EntityConfiguration> extends CopyOnWriteDescriptor {
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

    /**
     * Returns cloned descriptor with modified fields.
     *
     * @param values A fields to put into the new descriptor.
     * @return A new descriptor with modified fields.
     */
    @Override
    ConfigurationEntityRuntimeMetadata<E> setFields(final Map<String, ?> values);

    /**
     * Returns cloned descriptor with modified fields.
     *
     * @param values A fields to put into the new descriptor.
     * @return A new descriptor with modified fields.
     */
    @Override
    ConfigurationEntityRuntimeMetadata<E> setFields(final Descriptor values);
}
