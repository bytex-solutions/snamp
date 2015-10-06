package com.bytex.snamp.concurrent;

import com.bytex.snamp.TimeSpan;
import com.google.common.base.Stopwatch;

import java.util.concurrent.*;

/**
 * Represents synchronization primitive based on looping using
 * the custom condition.
 * @param <T> Type of the spinning result.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class SpinWait<T> implements Future<T> {
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
    private volatile Object result;

    /**
     * Represents default value of the spin delay.
     */
    protected static final TimeSpan DEFAULT_SPIN_DELAY = TimeSpan.ofMillis(1);

    protected SpinWait(final TimeSpan spinDelay){
        this.delayMillis = spinDelay.toMillis();
        state = new VolatileBox<>(SpinState.ACTIVE);
        result = null;
    }

    protected SpinWait(){
        this(DEFAULT_SPIN_DELAY);
    }

    /**
     * Gets an object used as indicator to break the spinning.
     * <p>
     *     Spinning will continue until this method return not {@literal null}.
     * </p>
     * @return An object used as indicator to break the spinning.
     * @throws Throwable An error occurred during execution of single spin action.
     */
    protected abstract T spin() throws Throwable;

    @SuppressWarnings("unchecked")
    private T checkState() throws ExecutionException, CancellationException{
        switch (state.get()){
            case CANCELLED: throw new CancellationException();
            case COMPLETED: return (T)result;
            case FAILED: throw new ExecutionException((Throwable)result);
            default: throw new IllegalStateException("Unexpected state ".concat(state.toString()));
        }
    }

    private T get(final long timeoutMillis) throws InterruptedException, ExecutionException, TimeoutException {
        final Stopwatch timer = Stopwatch.createStarted();
        while (!state.get().isDone) {
            final T result;
            try {
                result = spin();
            } catch (final Throwable e) {
                if (state.compareAndSet(SpinState.ACTIVE, SpinState.FAILED))
                    this.result = e;
                break;
            }
            if (result != null && state.compareAndSet(SpinState.ACTIVE, SpinState.COMPLETED))
                 this.result = result;
            else if (timer.elapsed(TimeUnit.MILLISECONDS) > timeoutMillis)
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
     * @see #spin()
     */
    @Override
    public final T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get(unit.toMillis(timeout));
    }

    /**
     * Blocks the caller thread (may be infinitely) until the event will not be raised.
     *
     * @return The event data.
     * @throws InterruptedException Waiting thread is aborted.
     * @throws ExecutionException Method {@link #spin()} raises an exception.
     * @see #spin()
     */
    @Override
    public final T get() throws InterruptedException, ExecutionException {
        while (!state.get().isDone) {
            final T result;
            try {
                result = spin();
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
