package com.snamp;

import java.util.concurrent.*;

/**
 * Represents standalone future which computation can be executed in the separated thread.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class FutureThread<V> extends Thread implements Future<V> {
    /**
     * Represents state of this thread.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    private static enum State{
        /**
         * Thread is created but not started.
         */
        CREATED,

        /**
         * Thread is running.
         */
        RUNNING,

        /**
         * Thread is cancelled.
         */
        CANCELLED,

        /**
         * Thread is completed.
         */
        COMPLETED
    }

    private final Callable<V> implementation;
    private volatile V result;
    private volatile Exception error;
    private volatile State st;

    /**
     * Initializes a new standalone future.
     * @param impl Implementation of the thread.
     */
    public FutureThread(final Callable<V> impl){
        if(impl == null) throw new IllegalArgumentException("impl is null.");
        implementation = impl;
        result = null;
        error = null;
        st = State.CREATED;
        setDaemon(true);
    }

    /**
     * Executes this thread.
     */
    @Override
    public final void run() {
        st = State.RUNNING;
        try {
            result = implementation.call();
        }
        catch (final InterruptedException e){
            st = State.CANCELLED;
        }
        catch (final Exception e) {
            error = e;
        }
        finally {
            st = isInterrupted() ? State.CANCELLED : State.COMPLETED;
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
        if(!mayInterruptIfRunning) return false;
        else if(isInterrupted()) return false;
        else switch (st){
                case RUNNING:
                    interrupt();
                    return true;
                default: return false;
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
        return st == State.CANCELLED;
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
        switch (st){
            case COMPLETED:
            case CANCELLED: return true;
            default: return false;
        }
    }

    private V getResult() throws ExecutionException{
        if(error != null) throw new ExecutionException(error);
        else return result;
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
    @Override
    public final V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        join(unit.toMillis(timeout));
        return getResult();
    }
}
