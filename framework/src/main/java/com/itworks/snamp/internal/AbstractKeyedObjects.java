package com.itworks.snamp.internal;

import java.util.HashMap;

/**
 * Represents a base class for constructing keyed collection.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractKeyedObjects<K, V> extends HashMap<K, V> implements KeyedObjects<K, V> {
    private static final long serialVersionUID = 330704774449284851L;

    /**
     * Initializes a new instance of the keyed collection.
     * @param capacity The initial capacity of this object.
     */
    protected AbstractKeyedObjects(final int capacity){
        super(capacity);
    }

    /**
     * Puts an item into this map.
     *
     * @param item An item to add into this map.
     * @return The previous item to be removed from this map.
     */
    @Override
    public final V put(final V item) {
        return put(getKey(item), item);
    }
}
