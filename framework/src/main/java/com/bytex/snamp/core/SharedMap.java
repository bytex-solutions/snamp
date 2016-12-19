package com.bytex.snamp.core;

import java.util.concurrent.ConcurrentMap;

/**
 * Represents shared map.
 */
public interface SharedMap extends ConcurrentMap<String, Object>, SharedObject {
}
