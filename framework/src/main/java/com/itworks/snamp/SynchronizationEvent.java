package com.itworks.snamp;

import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import static com.itworks.snamp.AbstractConcurrentResourceAccess.*;

/**
 * Represents synchronization event that is used to synchronize with some
 * asynchronous event.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class SynchronizationEvent<T> {
    /**
     * Represents awaitor of the synchronization event.
     * <p>
     * This interface should be used by event consumer.
     * </p>
     *
     * @param <T> Type of the event result.
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static interface Awaitor<T> {
        /**
         * Blocks the caller thread until the event will not be raised.
         *
         * @param timeout Event waiting timeout.
         * @return The event data.
         * @throws TimeoutException     timeout parameter too small for waiting.
         * @throws InterruptedException Waiting thread is aborted.
         */
        public T await(final TimeSpan timeout) throws TimeoutException, InterruptedException;

        /**
         * Blocks the caller thread (may be infinitely) until the event will not be raised.
         *
         * @return The event data.
         * @throws InterruptedException Waiting thread is aborted.
         */
        public T await() throws InterruptedException;
    }

    /**
     * Represents internal state of the synchronization event.
     * This class cannot be inherited.
     *
     * @param <T> Type of the event object.
     */
    private static final class EventState<T> extends AbstractQueuedSynchronizer implements Awaitor<T> {
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

        public final boolean set(final T result) {
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
            else throw new TimeoutException();
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

    private final ConcurrentResourceAccess<EventState<T>> state;
    private final boolean autoReset;

    /**
     * Initializes a new synchronization event.
     *
     * @param autoReset {@literal true} to reset synchronization event automatically after raising;
     *                  otherwise, {@literal false}.
     */
    public SynchronizationEvent(final boolean autoReset) {
        state = new ConcurrentResourceAccess<>(new EventState<T>());
        this.autoReset = autoReset;
    }

    /**
     * Initializes a new synchronization event.
     */
    public SynchronizationEvent() {
        this(false);
    }

    /**
     * Resets state of this event to initial.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final void reset() {
        state.changeResource(new EventState<T>());
    }

    /**
     * Fires the event.
     *
     * @param eventObj The raised event data.
     * @return {@literal true}, if this event is not in signalled state; otherwise, {@literal false}.
     */
    public final boolean fire(final T eventObj) {
        if (autoReset) {
            state.changeResource(new ConsistentAction<EventState<T>, EventState<T>>() {
                @Override
                public EventState<T> invoke(final EventState<T> state) {
                    state.set(eventObj);
                    return new EventState<>();
                }
            });
            return true;
        } else return state.write(new ConsistentAction<EventState<T>, Boolean>() {
            @Override
            public Boolean invoke(final EventState<T> state) {
                return state.set(eventObj);
            }
        });
    }

    /**
     * Determines whether this event is in signalled state.
     *
     * @return {@literal true}, if this event is in signalled state; otherwise, {@literal false}.
     */
    protected final boolean signalled() {
        return state.read(new ConsistentAction<EventState<T>, Boolean>() {
            @Override
            public Boolean invoke(final EventState<T> resource) {
                return resource.isSignalled();
            }
        });
    }

    /**
     * Creates a new awaitor for this event.
     *
     * @return A new awaitor for this event.
     */
    public final Awaitor<T> getAwaitor() {
        return state.getResource();
    }
}
