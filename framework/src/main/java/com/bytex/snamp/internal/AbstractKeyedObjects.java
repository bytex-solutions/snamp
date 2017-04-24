package com.bytex.snamp.internal;

import com.bytex.snamp.io.SerializableMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Represents a base class for constructing keyed collection.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractKeyedObjects<K, V> extends HashMap<K, V> implements KeyedObjects<K, V>, SerializableMap<K, V> {
    private static final long serialVersionUID = 330704774449284851L;

    /**
     * Initializes a new instance of the keyed collection.
     * @param capacity The initial capacity of this object.
     */
    protected AbstractKeyedObjects(final int capacity){
        super(capacity);
    }

    protected AbstractKeyedObjects(){
    }

    /**
     * Puts an item into this map.
     *
     * @param item An item to add into this map.
     * @return The previous item to be removed from this map.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public V put(final V item) {
        return put(getKey(item), item);
    }

    public static <K, V> AbstractKeyedObjects<K, V> create(final Function<? super V, ? extends K> keyMapper){
        return new AbstractKeyedObjects<K, V>() {
            private static final long serialVersionUID = -6817032232930472784L;

            @Override
            public K getKey(final V item) {
                return keyMapper.apply(item);
            }
        };
    }
}
