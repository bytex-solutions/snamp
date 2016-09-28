package com.bytex.snamp.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents cluster-local storage.
 * @since 2.0
 * @version 2.0
 */
final class LocalStorage extends ConcurrentHashMap<String, Object> {
    private static final long serialVersionUID = 2412615001344706359L;
}
