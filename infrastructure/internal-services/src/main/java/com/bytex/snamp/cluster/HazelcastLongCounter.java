package com.bytex.snamp.cluster;

import com.bytex.snamp.core.LongCounter;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

/**
 * Represents {@link com.bytex.snamp.core.LongCounter} backed by Hazelcast.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastLongCounter implements LongCounter {
    private final IAtomicLong counter;

    HazelcastLongCounter(final HazelcastInstance hazelcast, final String counterName){
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
