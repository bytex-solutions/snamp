package com.snamp;

import java.util.Map;

/**
 * Represents a type-safe map.
 * @author roman
 */
public interface TypeSafeMap<K, V> extends Map<K, V> {
    /**
     * Returns the type of the map keys.
     * @return
     */
    public Class<K> getKeyType();

    /**
     * Returns the type of the map values.
     * @return
     */
    public Class<V> getValueType();
}
