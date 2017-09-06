package com.bytex.snamp.cluster;

import com.bytex.snamp.core.SharedObject;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
abstract class GridSharedObject implements SharedObject {
    private final String name;

    GridSharedObject(final String name){
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    abstract void destroy();

    final IllegalStateException objectIsDestroyed() {
        return new IllegalStateException(String.format("Shared object %s is destroyed", getName()));
    }
}
