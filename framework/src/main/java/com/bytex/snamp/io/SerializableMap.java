package com.bytex.snamp.io;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a contract for serializable map.
 * @param <K> The type of keys maintained by this map
 * @param <V> The type of mapped values
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface SerializableMap<K, V> extends Map<K, V>, Serializable {
}
