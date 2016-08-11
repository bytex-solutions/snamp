package com.bytex.snamp.configuration;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    default void consumeOrAdd(final String entityID, final Consumer<? super E> handler){
        handler.accept(getOrAdd(entityID));
    }

    default <I> void consumeOrAdd(final I input, final String entityID, final BiConsumer<? super I, ? super E> handler){
        handler.accept(input, getOrAdd(entityID));
    }
}
