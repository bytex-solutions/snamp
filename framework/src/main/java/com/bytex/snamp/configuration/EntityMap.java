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

    /**
     * Processes entity configuration contained in this collection.
     * @param entityID Unique identifier of entity to process. Cannot be {@literal null}.
     * @param handler A function used to process configuration. Cannot be {@literal null}.
     * @return {@literal true} if entity was added; {@literal false} if entity was exist.
     * @since 2.0
     */
    default boolean addAndConsume(final String entityID, final Consumer<? super E> handler) {
        if (containsKey(entityID)) {
            handler.accept(get(entityID));
            return false;
        } else {
            handler.accept(getOrAdd(entityID));
            return true;
        }
    }

    default <I> boolean addAndConsume(final I input, final String entityID, final BiConsumer<? super I, ? super E> handler){
        return addAndConsume(entityID, entity -> handler.accept(input, entity));
    }
}
