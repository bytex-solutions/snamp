package com.bytex.snamp.core.cluster;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class InMemoryStorage extends ConcurrentHashMap<String, Object> {
    private static final long serialVersionUID = 7359500906547128868L;

    InMemoryStorage() {

    }
}
