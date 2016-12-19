package com.bytex.snamp.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents cluster-local storage.
 * @since 2.0
 * @version 2.0
 */
final class LocalStorage extends ConcurrentHashMap<String, Object> implements SharedMap {
    private static final long serialVersionUID = 2412615001344706359L;
    private final String name;

    LocalStorage(final String name){
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }
}
