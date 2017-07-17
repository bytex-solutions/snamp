package com.bytex.snamp;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents specialized map with key of type {@link Class}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
final class ClassMap<V> extends ConcurrentHashMap<Class<?>, V> {
    private static final long serialVersionUID = 3041733574645762326L;

    /**
     * Special version of {@link #get(Object)} method that can modify the map.
     * <p>
     *     If value associated with the specified key doesn't exist then method tries to find the value
     *     using inheritance hierarchy of the {@link Class} passed as key.
     * @param key Key
     * @return Value associated with the key.
     */
    V getOrAdd(final Class<?> key) {
        for (Class<?> lookup = key; lookup != null; lookup = lookup.getSuperclass()) {
            final V value = get(lookup);
            if (value != null) {
                //cache converter for the origin key, not for current inheritance frame
                putIfAbsent(key, value);
                return value;
            }
        }
        return null;
    }
}
