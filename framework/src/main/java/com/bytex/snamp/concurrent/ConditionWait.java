package com.bytex.snamp.concurrent;

import com.bytex.snamp.TimeSpan;

/**
 * Represents spin wait based on the periodic condition check.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ConditionWait<E extends Throwable> extends SpinWait<Object, E> {

    protected ConditionWait(final TimeSpan spinDelay) {
        super(spinDelay);
    }

    protected ConditionWait() {
        this(DEFAULT_SPIN_DELAY);
    }

    /**
     * Do conditional check.
     *
     * @return {@literal true} to change the state of this spin wait to signalled; otherwise, {@literal false}.
     * @throws E Internal condition check error.
     */
    protected abstract boolean checkCondition() throws E;

    /**
     * Gets an object used as indicator to break the spinning.
     * <p>
     * Spinning will continue until this method return not {@literal null}.
     * </p>
     *
     * @return An object used as indicator to break the spinning.
     * @throws E Internal checker error.
     */
    @Override
    protected final Object get() throws E {
        return checkCondition() ? this : null;
    }
}
