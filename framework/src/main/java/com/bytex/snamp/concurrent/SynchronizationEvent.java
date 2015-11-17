package com.bytex.snamp.concurrent;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Represents synchronization event that is used to synchronize with some
 * asynchronous event.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class SynchronizationEvent<T> {

    /**
     * Represents internal state of the synchronization event.
     * This class cannot be inherited.
     *
     * @param <T> Type of the event object.
     */
    private static final class EventState<T> extends AbstractFuture<T> {
        @Override
        public boolean set(final T value) {
            return super.set(value);
        }

        @Override
        public boolean setException(final Throwable throwable) {
            return super.setException(throwable);
        }
    }

    private volatile EventState<T> state;
    private final boolean autoReset;
    private static final boolean DEFAULT_AUTO_RESET = false;

    /**
     * Initializes a new synchronization event.
     *
     * @param autoReset {@literal true} to reset synchronization event automatically after raising;
     *                  otherwise, {@literal false}.
     */
    public SynchronizationEvent(final boolean autoReset) {
        state = new EventState<>();
        this.autoReset = autoReset;
    }

    /**
     * Initializes a new synchronization event.
     */
    public SynchronizationEvent() {
        this(DEFAULT_AUTO_RESET);
    }

    /**
     * Resets state of this event to initial.
     */
    public synchronized final void reset() {
        state = new EventState<>();
    }

    /**
     * Fires the event.
     *
     * @param eventObj The raised event data.
     * @return {@literal true}, if this event is not in signalled state; otherwise, {@literal false}.
     */
    public synchronized final boolean fire(final T eventObj) {
        final boolean result = state.set(eventObj);
        if (autoReset)
            state = new EventState<>();
        return result;
    }

    public synchronized final boolean raise(final Throwable e){
        final boolean result = state.setException(e);
        if (autoReset)
            state = new EventState<>();
        return result;
    }

    /**
     * Creates a new awaitor for this event.
     *
     * @return A new awaitor for this event.
     */
    public final ListenableFuture<T> getAwaitor() {
        return state;
    }
}
