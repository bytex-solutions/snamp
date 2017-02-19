package com.bytex.snamp.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Objects;

/**
 * Represents lightweight timer that is used to repeat processing of the specified object using weak reference to it.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public abstract class WeakRepeater<I> extends Repeater {
    private final WeakReference<I> ref;

    /**
     * Initializes a new repeater.
     *
     * @param period Time between successive task executions. Cannot be {@literal null}.
     * @param input Input object to process it periodically. Cannot be {@literal null}.
     * @throws IllegalArgumentException period is {@literal null}.
     */
    protected WeakRepeater(final Duration period, final I input) {
        super(period);
        ref = new WeakReference<>(Objects.requireNonNull(input));
    }

    /**
     * Initializes a new repeater.
     *
     * @param period Time between successive task executions, in millis.
     * @param input Input object to process it periodically. Cannot be {@literal null}.
     */
    protected WeakRepeater(final long period, final I input) {
        super(period);
        ref = new WeakReference<>(Objects.requireNonNull(input));
    }

    /**
     * Clears a weak reference to the object participated in processing.
     *
     * @param s A new repeater state.
     */
    @Override
    protected void stateChanged(final RepeaterState s) {
        if(RepeaterState.CLOSED.equals(s))
            ref.clear();
    }

    protected abstract void doAction(final I input) throws Exception;

    /**
     * Provides some periodical action.
     *
     * @throws Exception Action is failed.
     */
    @Override
    protected final void doAction() throws Exception {
        final I input = ref.get();
        if (input == null)
            Thread.currentThread().interrupt();
        else
            doAction(input);
    }
}
