package com.bytex.snamp.concurrent;

import com.google.common.base.Stopwatch;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Represents synchronization primitive based on looping using
 * the custom condition.
 * This class cannot be inherited.
 * @param <T> Type of the spinning result.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class SpinWait<T> implements Future<T> {
    private enum SpinState {
        ACTIVE(false),
        CANCELLED(true),
        FAILED(true),
        COMPLETED(true);

        private final boolean isDone;

        SpinState(final boolean done){
            this.isDone = done;
        }
    }

    private final long delayMillis;
    private final VolatileBox<SpinState> state;
    private final Callable<? extends T> spin;
    private volatile Object result;

    /**
     * Represents default value of the spin delay.
     */
    static final Duration DEFAULT_SPIN_DELAY = Duration.ofMillis(1);

    SpinWait(final Callable<? extends T> spin, final Duration spinDelay){
        this.delayMillis = spinDelay.toMillis();
        state = new VolatileBox<>(SpinState.ACTIVE);
        result = null;
        this.spin = Objects.requireNonNull(spin);
    }

    public static <T> SpinWait<T> create(final Callable<? extends T> spin){
        return new SpinWait<>(spin, DEFAULT_SPIN_DELAY);
    }

    public static <T> SpinWait<T> create(final Callable<? extends T> spin, final Duration spinDelay){
        return new SpinWait<>(spin, spinDelay);
    }

    @SuppressWarnings("unchecked")
    private T checkState() throws ExecutionException, CancellationException{
        switch (state.get()){
            case CANCELLED: throw new CancellationException();
            case COMPLETED: return (T)result;
            case FAILED: throw new ExecutionException((Throwable)result);
            default: throw new IllegalStateException("Unexpected state ".concat(state.toString()));
        }
    }

    private T get(final long timeoutNanos) throws InterruptedException, ExecutionException, TimeoutException {
        final Stopwatch timer = Stopwatch.createStarted();
        while (!state.get().isDone) {
            final T result;
            try {
                result = spin.call();
            } catch (final Throwable e) {
                if (state.compareAndSet(SpinState.ACTIVE, SpinState.FAILED))
                    this.result = e;
                break;
            }
            if (result != null && state.compareAndSet(SpinState.ACTIVE, SpinState.COMPLETED))
                 this.result = result;
            else if (timer.elapsed(TimeUnit.NANOSECONDS) >= timeoutNanos)
                throw new TimeoutException("Spin wait timed out");
            else if (Thread.interrupted()) throw spinWaitInterrupted();
            else Thread.sleep(delayMillis);
        }
        return checkState();
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException    if the computation threw an
     *                               exception
     * @throws InterruptedException  if the current thread was interrupted
     *                               while waiting
     * @throws TimeoutException      if the wait timed out
     */
    @Override
    public final T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get(unit.toNanos(timeout));
    }

    /**
     * Blocks the caller thread (may be infinitely) until the event will not be raised.
     *
     * @return The event data.
     * @throws InterruptedException Waiting thread is aborted.
     * @throws ExecutionException Spin method raises an exception.
     */
    @Override
    public final T get() throws InterruptedException, ExecutionException {
        while (!state.get().isDone) {
            final T result;
            try {
                result = spin.call();
            } catch (final Throwable e) {
                if (state.compareAndSet(SpinState.ACTIVE, SpinState.FAILED))
                    this.result = e;
                break;
            }
            if (result != null && state.compareAndSet(SpinState.ACTIVE, SpinState.COMPLETED))
                this.result = result;
            else if (Thread.interrupted()) throw spinWaitInterrupted();
            else Thread.sleep(delayMillis);
        }
        return checkState();
    }

    private static InterruptedException spinWaitInterrupted(){
        return new InterruptedException("SpinWait interrupted");
    }

    /**
     * Cancels awaiting.
     * @param mayInterruptIfRunning Not used.
     * @return {@literal true}, if awaiting is cancelled successfully; {@literal false}, if previously cancelled.
     */
    @Override
    public final boolean cancel(final boolean mayInterruptIfRunning) {
        return state.compareAndSet(SpinState.ACTIVE, SpinState.CANCELLED);
    }

    /**
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally.
     *
     * @return <tt>true</tt> if this task was cancelled before it completed
     */
    @Override
    public final boolean isCancelled() {
        return state.get() == SpinState.CANCELLED;
    }

    /**
     * Returns <tt>true</tt> if this task completed.
     * <p/>
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * <tt>true</tt>.
     *
     * @return <tt>true</tt> if this task completed
     */
    @Override
    public final boolean isDone() {
        return state.get().isDone;
    }


}
