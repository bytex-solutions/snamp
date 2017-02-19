package com.bytex.snamp;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents a map that can create values as a factory.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface FactoryMap<K, V> extends Map<K, V> {
    /**
     * Gets or adds a new value.
     * @param key Key of the item.
     * @return Existing or newly created value of associated with the key.
     */
    V getOrAdd(final K key);

    default boolean addAndConsume(final K key, final Consumer<? super V> handler) {
        if (containsKey(key)) {
            handler.accept(get(key));
            return false;
        } else {
            handler.accept(getOrAdd(key));
            return true;
        }
    }

    default <I> boolean addAndConsume(final I input, final K key, final BiConsumer<? super I, ? super V> handler) {
        return addAndConsume(key, entity -> handler.accept(input, entity));
    }
}
