package com.bytex.snamp.configuration;

import java.util.Map;

/**
 * Represents catalog of configuration entities.
 * @param <E> Type of the configuration entities in the catalog.
 */
public interface EntityMap<E extends EntityConfiguration> extends Map<String, E> {
    /**
     * Gets existing configuration entity; or creates and registers a new entity.
     * @param entityID Identifier of the configuration entity.
     * @return Configuration entity from the catalog.
     */
    E getOrAdd(final String entityID);
}
