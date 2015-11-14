package com.bytex.snamp.core.cluster;

import com.bytex.snamp.core.SequenceNumberGenerator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HazelcastSequenceNumberGenerator implements SequenceNumberGenerator {
    private IAtomicLong underlyingGenerator;

    HazelcastSequenceNumberGenerator(final HazelcastInstance hazelcast, final String generatorName){
        underlyingGenerator = hazelcast.getAtomicLong(generatorName);
    }

    /**
     * Generates a new cluster-wide unique identifier.
     *
     * @return A new cluster-wide unique identifier.
     */
    @Override
    public long next() {
        return underlyingGenerator.getAndIncrement();
    }

    public static void release(final HazelcastInstance hazelcast, final String generatorName) {
        hazelcast.getIdGenerator(generatorName).destroy();
    }
}
