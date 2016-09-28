package com.bytex.snamp.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents cluster-local counter.
 * @since 2.0
 * @version 2.0
 */
final class LocalLongCounter extends AtomicLong implements LongCounter {
    private static final long serialVersionUID = 498408165929062468L;

    LocalLongCounter() {
        super(0L);
    }

    @Override
    public long getAsLong() {
        return getAndIncrement();
    }
}
