package com.itworks.snamp.concurrent;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.core.LogicalOperation;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Represents synchronization event that is used to synchronize with some
 * asynchronous event.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class SynchronizationEvent<T> {
    /**
     * Represents synchronization event awaitor.
     * @param <T> Type of the event.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public interface EventAwaitor<T> extends Awaitor<T, ExceptionPlaceholder>{
        /**
         * Blocks the caller thread until the event will not be raised.
         *
         * @param timeout Event waiting timeout.
         * @return The event data.
         * @throws java.util.concurrent.TimeoutException timeout parameter too small for waiting.
         * @throws InterruptedException                  Waiting thread is aborted.
         */
        @Override
        T await(final TimeSpan timeout) throws TimeoutException, InterruptedException;

        /**
         * Blocks the caller thread (may be infinitely) until the event will not be raised.
         *
         * @return The event data.
         * @throws InterruptedException Waiting thread is aborted.
         */
        @Override
        T await() throws InterruptedException;
    }

    /**
     * Represents internal state of the synchronization event.
     * This class cannot be inherited.
     *
     * @param <T> Type of the event object.
     */
    private static final class EventState<T> extends AbstractQueuedSynchronizer implements EventAwaitor<T> {
        private static final long serialVersionUID = 7883990808552287879L;
        private T eventObj;

        public EventState() {
            setState(0);
            eventObj = null;
        }

        private boolean isSignalled() {
            return getState() != 0;
        }

        protected int tryAcquireShared(int ignore) {
            return isSignalled() ? 1 : -1;
        }

        protected boolean tryReleaseShared(int ignore) {
            setState(1);
            return true;
        }

        private boolean set(final T result) {
            if (isSignalled()) return false;
            this.eventObj = result;
            return releaseShared(1);
        }

        /**
         * Blocks the caller thread until the event will not be raised.
         *
         * @param timeout Event waiting timeout.
         * @return The event data.
         * @throws java.util.concurrent.TimeoutException timeout parameter too small for waiting.
         * @throws InterruptedException                  Waiting thread is aborted.
         */
        @Override
        public T await(final TimeSpan timeout) throws TimeoutException, InterruptedException {
            if (timeout == TimeSpan.INFINITE) return await();
            else if (tryAcquireSharedNanos(1, timeout.toNanos())) return eventObj;
            else throw new TimeoutException(String.format("Event timed out. Context: %s", LogicalOperation.current()));
        }

        /**
         * Blocks the caller thread (may be infinitely) until the event will not be raised.
         *
         * @return The event data.
         * @throws InterruptedException Waiting thread is aborted.
         */
        @Override
        public T await() throws InterruptedException {
            acquireSharedInterruptibly(1);
            return eventObj;
        }
    }

    private volatile EventState<T> state;
    private final boolean autoReset;
    private static boolean DEFAULT_AUTO_RESET = false;

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

    /**
     * Determines whether this event is in signalled state.
     *
     * @return {@literal true}, if this event is in signalled state; otherwise, {@literal false}.
     */
    protected final boolean signalled() {
        return state.isSignalled();
    }

    /**
     * Creates a new awaitor for this event.
     *
     * @return A new awaitor for this event.
     */
    public final EventAwaitor<T> getAwaitor() {
        return state;
    }

    protected final <E extends Throwable> EventAwaitor<T> getAwaitor(final Consumer<? super SynchronizationEvent<T>, E> handler) throws E{
        final EventAwaitor<T> awaitor = getAwaitor();
        handler.accept(this);
        return awaitor;
    }

    public static <T, E extends Throwable> EventAwaitor<T> processEvent(final Consumer<? super SynchronizationEvent<T>, E> handler,
                                                                 final boolean autoReset) throws E{
        final SynchronizationEvent<T> event = new SynchronizationEvent<>(autoReset);
        return event.getAwaitor(handler);
    }

    public static <T, E extends Throwable> EventAwaitor<T> processEvent(final Consumer<? super SynchronizationEvent<T>, E> handler) throws E{
        return processEvent(handler, DEFAULT_AUTO_RESET);
    }
}
