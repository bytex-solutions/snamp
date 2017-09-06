package com.bytex.snamp.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents cluster-local counter.
 * @since 2.0
 * @version 2.1
 */
final class InMemoryCounter extends AtomicLong implements SharedCounter {
    private static final long serialVersionUID = 498408165929062468L;
    private final String name;

    InMemoryCounter(final String name){
        super(0L);
        this.name = name;
    }

    @Override
    public long getAsLong() {
        return getAndIncrement();
    }

    @Override
    public String getName() {
        return name;
    }
}
