package com.bytex.snamp.cluster;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * Represents shared object based on Hazelcast distributed object.
 */
abstract class HazelcastSharedObject<S extends DistributedObject> extends GridSharedObject {
    private final AtomicReference<S> distributedObject;

    HazelcastSharedObject(final HazelcastInstance hazelcast, final String objectName, final BiFunction<? super HazelcastInstance, ? super String, ? extends S> objectProvider) {
        distributedObject = new AtomicReference<>(objectProvider.apply(hazelcast, objectName));
    }

    final S getDistributedObject() {
        final S result = distributedObject.get();
        if (result == null)
            throw objectIsDestroyed();
        else
            return result;
    }

    @Override
    public final String getName() {
        return getDistributedObject().getName();
    }

    @Override
    final void destroy() {
        final S obj = distributedObject.getAndSet(null);
        if (obj != null)
            obj.destroy();
    }
}
