package com.bytex.snamp.cluster;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;

import java.util.function.BiFunction;

/**
 * Represents shared object based on Hazelcast distributed object.
 */
abstract class HazelcastSharedObject<S extends DistributedObject> extends GridSharedObject {
    final S distributedObject;

    HazelcastSharedObject(final HazelcastInstance hazelcast, final String objectName, final BiFunction<? super HazelcastInstance, ? super String, ? extends S> objectProvider){
        distributedObject = objectProvider.apply(hazelcast, objectName);
    }

    @Override
    public final String getName() {
        return distributedObject.getName();
    }

    @Override
    final void destroy() {
        distributedObject.destroy();
    }

    @Override
    public final boolean isPersistent() {
        return false;
    }
}
