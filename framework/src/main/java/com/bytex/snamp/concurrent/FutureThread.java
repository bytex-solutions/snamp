package com.bytex.snamp.concurrent;

import java.util.concurrent.*;

/**
 * Represents standalone future which computation can be executed in the separated thread.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class FutureThread<V> extends Thread implements Future<V>{
    /**
     * Represents state of this thread.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    private enum ThreadState {
        /**
         * Thread is created but not started.
         */
        CREATED(false),

        /**
         * Thread is running.
         */
        RUNNING(false),

        /**
         * Thread is cancelled.
         */
        CANCELLED(true),

        /**
         * Thread throws exception.
         */
        FAILED(true),

        /**
         * Thread is completed.
         */
        COMPLETED(true);

        private final boolean isDone;

        ThreadState(final boolean done){
            this.isDone = done;
        }
    }

    private final Callable<V> implementation;
    private volatile Object result;
    private final VolatileBox<ThreadState> state;

    /**
     * Initializes a new standalone future.
     * @param impl Implementation of the thread. Cannot be {@literal null}.
     */
    public FutureThread(final Callable<V> impl){
        this(null, impl);
    }

    /**
     * Initializes a new standalone future.
     * @param group Thread group for this future.
     * @param impl Implementation of the thread. Cannot be {@literal null}.
     */
    public FutureThread(final ThreadGroup group, final Callable<V> impl){
        super(group, impl.toString());
        implementation = impl;
        state = new VolatileBox<>(ThreadState.CREATED);
        setDaemon(true);
    }

    /**
     * Executes a new task in the separated thread.
     * @param task The task to apply in the separated thread.
     * @param <V> Type of the asynchronous computation result.
     * @return An object that represents the state of asynchronous computation.
     */
    public static <V> FutureThread<V> start(final Callable<V> task) {
        final FutureThread<V> future = new FutureThread<>(task);
        future.start();
        return future;
    }

    /**
     * Executes this thread.
     */
    @Override
    public final void run() {
        if (state.compareAndSet(ThreadState.CREATED, ThreadState.RUNNING)) {
            try {
                result = implementation.call();
            } catch (final InterruptedException e) {
                state.set(ThreadState.CANCELLED);
                result = null;
                return;
            } catch (final Exception e) {
                state.set(ThreadState.FAILED);
                result = e;
                return;
            }
            state.set(isInterrupted() ? ThreadState.CANCELLED : ThreadState.COMPLETED);
        }
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when <tt>cancel</tt> is called,
     * this task should never run.  If the task has already started,
     * then the <tt>mayInterruptIfRunning</tt> parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     * <p/>
     * <p>After this method returns, subsequent calls to {@link #isDone} will
     * always return <tt>true</tt>.  Subsequent calls to {@link #isCancelled}
     * will always return <tt>true</tt> if this method returned <tt>true</tt>.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     *                              task should be interrupted; otherwise, in-progress tasks are allowed
     *                              to complete
     * @return <tt>false</tt> if the task could not be cancelled,
     *         typically because it has already completed normally;
     *         <tt>true</tt> otherwise
     */
    @Override
    public final boolean cancel(final boolean mayInterruptIfRunning) {
        if (isInterrupted()) return false;
        switch (state.getAndSet(ThreadState.CANCELLED)) {
            case RUNNING:
                if(mayInterruptIfRunning) {
                    interrupt();
                    return true;
                }
                else return false;
            case CREATED:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally.
     *
     * @return <tt>true</tt> if this task was cancelled before it completed
     */
    @Override
    public final boolean isCancelled() {
        return state.get() == ThreadState.CANCELLED;
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

    @SuppressWarnings("unchecked")
    private V getResult() throws ExecutionException {
        final ThreadState st;
        switch (st = state.get()) {
            case FAILED:
                throw new ExecutionException((Throwable) result);
            case COMPLETED:
                return (V) result;
            case CANCELLED:
                throw new CancellationException();
            default:
                throw new IllegalStateException("Incorrect state" + st);
        }
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws java.util.concurrent.CancellationException
     *                              if the computation was cancelled
     * @throws java.util.concurrent.ExecutionException
     *                              if the computation threw an
     *                              exception
     * @throws InterruptedException if the current thread was interrupted
     *                              while waiting
     */
    @Override
    public final V get() throws InterruptedException, ExecutionException {
        join();
        return getResult();
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the computed result
     * @throws java.util.concurrent.CancellationException
     *                              if the computation was cancelled
     * @throws java.util.concurrent.ExecutionException
     *                              if the computation threw an
     *                              exception
     * @throws InterruptedException if the current thread was interrupted
     *                              while waiting
     * @throws java.util.concurrent.TimeoutException
     *                              if the wait timed out
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public final V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        join(unit.toMillis(timeout));
        return getResult();
    }
}
