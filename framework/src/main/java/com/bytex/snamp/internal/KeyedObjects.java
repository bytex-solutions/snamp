package com.bytex.snamp.internal;

import java.util.Map;

/**
 * Represents a map of keyed objects where unique key is based on the item content.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface KeyedObjects<K, V> extends Map<K, V> {
    /**
     * Computes key for the specified item.
     * <p>
     *     This method is stateless.
     * </p>
     * @param item An item to compute.
     * @return A key computed for the specified item.
     */
    K getKey(final V item);

    /**
     * Puts an item into this map.
     * @param item An item to add into this map.
     * @return The previous item to be removed from this map.
     */
    V put(final V item);
}
