package com.bytex.snamp.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents cluster-local counter.
 * @since 2.0
 * @version 2.0
 */
final class LocalCounter extends AtomicLong implements SharedCounter {
    private static final long serialVersionUID = 498408165929062468L;
    private final String counterName;

    LocalCounter(final String name) {
        super(0L);
        counterName = name;
    }

    @Override
    public long getAsLong() {
        return getAndIncrement();
    }

    @Override
    public String getName() {
        return counterName;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }
}
