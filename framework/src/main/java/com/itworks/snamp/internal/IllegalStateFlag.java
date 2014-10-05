package com.itworks.snamp.internal;

import com.itworks.snamp.WriteOnceRef;
import com.itworks.snamp.internal.annotations.Internal;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import org.apache.commons.collections4.Factory;

/**
 * Represents object state flag that can be aggregated inside of stateful objects.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public abstract class IllegalStateFlag extends WriteOnceRef<Boolean> implements Factory<IllegalStateException> {
    /**
     * Initializes a new state flag initially set to {@literal false}.
     */
    protected IllegalStateFlag(){
        super(false);
    }

    /**
     * Verifies that this state is legal.
     * @throws java.lang.IllegalStateException Occurs if state of this container is {@literal true}.
     */
    @ThreadSafe
    public final void verify(){
        if(get()) throw create();
    }

    /**
     * Sets the illegal object state to {@literal true}.
     * @return {@literal true}, if container is changed successfully; otherwise, {@literal false}.
     */
    @ThreadSafe
    public final boolean set(){
        return set(true);
    }
}
