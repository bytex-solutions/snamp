package com.bytex.snamp.cluster;

import com.bytex.snamp.core.SharedObject;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class GridSharedObject implements SharedObject {
    abstract void destroy();

    final IllegalStateException objectIsDestroyed() {
        return new IllegalStateException(String.format("Shared object %s is destroyed", this));
    }
}
