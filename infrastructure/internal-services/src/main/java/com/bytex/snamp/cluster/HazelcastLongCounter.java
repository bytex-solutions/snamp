package com.bytex.snamp.cluster;

import com.bytex.snamp.core.LongCounter;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class HazelcastLongCounter implements LongCounter {
    private IAtomicLong underlyingGenerator;

    HazelcastLongCounter(final HazelcastInstance hazelcast, final String generatorName){
        underlyingGenerator = hazelcast.getAtomicLong(generatorName);
    }

    /**
     * Generates a new cluster-wide unique identifier.
     *
     * @return A new cluster-wide unique identifier.
     */
    @Override
    public long increment() {
        return underlyingGenerator.getAndIncrement();
    }

    public static void release(final HazelcastInstance hazelcast, final String generatorName) {
        hazelcast.getIdGenerator(generatorName).destroy();
    }
}
