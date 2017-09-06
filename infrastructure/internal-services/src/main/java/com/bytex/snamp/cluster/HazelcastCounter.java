package com.bytex.snamp.cluster;

import com.bytex.snamp.core.SharedCounter;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

/**
 * Represents {@link SharedCounter} backed by Hazelcast.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class HazelcastCounter extends HazelcastSharedObject<IAtomicLong> implements SharedCounter {

    HazelcastCounter(final HazelcastInstance hazelcast, final String name){
        super(hazelcast, name, HazelcastInstance::getAtomicLong);
    }

    @Override
    public long getAsLong() {
        return getDistributedObject().getAndIncrement();
    }
}
