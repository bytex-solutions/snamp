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
final class HazelcastCounter extends HazelcastSharedObject<IAtomicLong> implements SharedCounter {

    HazelcastCounter(final HazelcastInstance hazelcast, final String counterName){
        super(hazelcast, counterName, HazelcastInstance::getAtomicLong);
    }

    @Override
    public long getAsLong() {
        return getDistributedObject().getAndIncrement();
    }
}
