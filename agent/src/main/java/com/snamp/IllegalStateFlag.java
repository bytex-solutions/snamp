package com.snamp;

import com.snamp.internal.Internal;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;

/**
 * Represents object state flag that can be aggregated inside of stateful objects.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public abstract class IllegalStateFlag extends WriteOnceRef<Boolean> implements Activator<IllegalStateException> {
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
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final void verify(){
        if(get()) throw newInstance();
    }

    /**
     * Sets the illegal object state to {@literal true}.
     * @return {@literal true}, if container is changed successfully; otherwise, {@literal false}.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final boolean set(){
        return set(true);
    }
}
