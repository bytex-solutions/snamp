package com.bytex.snamp.configuration;

import com.bytex.snamp.FactoryMap;

import java.util.Optional;

/**
 * Represents catalog of configuration entities.
 * @param <E> Type of the configuration entities in the catalog.
 */
public interface EntityMap<E extends EntityConfiguration> extends FactoryMap<String, E> {
    /**
     * Gets existing configuration entity; or creates and registers a new entity.
     * @param entityID Identifier of the configuration entity.
     * @return Configuration entity from the catalog.
     */
    @Override
    E getOrAdd(final String entityID);

    default Optional<E> getIfPresent(final String entityID){
        return containsKey(entityID) ? Optional.of(get(entityID)) : Optional.empty();
    }

    default void putAll(final EntityMap<?> entities) {
        entities.forEach((name, importedEntity) -> getOrAdd(name).load(importedEntity));
    }
}
