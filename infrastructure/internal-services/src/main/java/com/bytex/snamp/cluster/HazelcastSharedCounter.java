package com.bytex.snamp.cluster;

import com.bytex.snamp.core.SharedCounter;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

/**
 * Represents {@link SharedCounter} backed by Hazelcast.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastSharedCounter implements SharedCounter {
    private final IAtomicLong counter;

    HazelcastSharedCounter(final HazelcastInstance hazelcast, final String counterName){
        counter = hazelcast.getAtomicLong(counterName);
    }

    @Override
    public String getName() {
        return counter.getName();
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public long getAsLong() {
        return counter.getAndIncrement();
    }
}
