package com.bytex.snamp.cluster;

import com.bytex.snamp.SpecialUse;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.function.BiFunction;

/**
 * Represents shared object based on Hazelcast distributed object.
 */
abstract class HazelcastSharedObject<S extends DistributedObject> extends GridSharedObject {
    @SpecialUse(SpecialUse.Case.JVM)
    private S distributedObject;

    HazelcastSharedObject(final HazelcastInstance hazelcast,
                          final String distributedObjectName,
                          final BiFunction<? super HazelcastInstance, ? super String, ? extends S> objectProvider) {
        super(distributedObjectName);
        distributedObject = objectProvider.apply(hazelcast, distributedObjectName);
    }

    final S getDistributedObject() {
        if (distributedObject == null)
            throw objectIsDestroyed();
        else
            return distributedObject;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    void destroy() {
        if(distributedObject != null)
            distributedObject.destroy();
        distributedObject = null;
    }
}
