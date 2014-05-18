package com.itworks.snamp;

import com.itworks.snamp.internal.semantics.MethodStub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents lightweight timer that is used to repeat some action in time.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class Repeater implements AutoCloseable, Runnable {
    /**
     * Represents state of this timer.
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static enum State{
        /**
         * The timer is started.
         */
        STARTED,

        /**
         * The timer is in stopping state.
         */
        STOPPING,

        /**
         * The timer is stopped.
         */
        STOPPED,

        /**
         * The timer is stopped because it throws an exception.
         */
        FAILED
    }

    private State state;
    private Throwable exception;
    private final TimeSpan period;
    private RepeaterThread repeatThread;

    /**
     * Initializes a new repeater.
     * @param period Time between successive task executions. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException period is {@literal null}.
     */
    protected Repeater(final TimeSpan period){
        if(period == null) throw new IllegalArgumentException("period is null.");
        this.state = State.STOPPED;
        this.period = period;
        this.exception = null;
        this.repeatThread = null;
    }

    /**
     * Returns time between successive task executions.
     * @return Time between successive task executions.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final TimeSpan getPeriod(){
        return period;
    }

    /**
     * Returns the state of this repeater.
     * @return The state of this repeater.
     * @see Repeater.State
     */
    public final State getState(){
        return state;
    }

    /**
     * Returns an exception thrown by the repeatable action.
     * @return An exception thrown by the repeatable action.
     */
    public final Throwable getException(){
        return exception;
    }

    /**
     * Handles a new repeater state.
     * <p>
     *     In the default implementation this method does nothing.
     *     You can override this method to handle the repeater state.
     * </p>
     * @param s A new repeater state.
     */
    @SuppressWarnings("UnusedParameters")
    @MethodStub
    protected void stateChanged(final State s){

    }

    /**
     * Informs this repeater about unhandled exception in the repeatable action.
     * <p>
     *     This method can be called only from timer thread (from {@link #doAction() method}.
     * </p>
     * @param e An exception occured in the repeatable action.
     * @throws java.lang.IllegalStateException This repeater is not in {@link State#STARTED} state;
     *  or this method is not called from the {@link #doAction()} method.
     */
    protected synchronized final void fault(final Throwable e){
        switch (state){
            case STARTED:
                if(repeatThread.getId() == Thread.currentThread().getId()){
                    exception = e;
                    stateChanged(state = State.FAILED);
                    Thread.currentThread().interrupt();
                }
                else throw new IllegalStateException("This method should be called from the timer action.");
            break;
            default:
                throw new IllegalStateException(String.format("The repeater must be in %s state but actual state is %s", State.STARTED, state));
        }
    }

    /**
     * Provides some periodical action.
     */
    protected abstract void doAction();

    private static interface RepeaterWorker extends Runnable, Thread.UncaughtExceptionHandler{

    }

    private static interface RepeaterThread extends Runnable{
        void start();
        void interrupt();
        long getId();
        void join(final TimeSpan timeout) throws InterruptedException, TimeoutException;
    }

    private final static class RepeaterThreadImpl extends Thread implements RepeaterThread{
        private final long period;

        public RepeaterThreadImpl(final RepeaterWorker worker, final TimeSpan period){
            super(worker);
            this.period = period.convert(TimeUnit.MILLISECONDS).duration;
            setDaemon(true);
            setUncaughtExceptionHandler(worker);
        }

        @Override
        public final void run() {
            while (!Thread.interrupted()){
                //sleep for a specified time
                try{
                    Thread.sleep(period);
                }
                catch (final InterruptedException e){
                    return;
                }
                super.run();
            }
        }

        @Override
        public final void join(final TimeSpan timeout) throws InterruptedException, TimeoutException{
            if(timeout == TimeSpan.INFINITE) join();
            else join(timeout.convert(TimeUnit.MILLISECONDS).duration);
            if(isAlive()) throw new TimeoutException("Thread is not stopped.");
        }
    }

    /**
     * Executes the repeater.
     * @throws java.lang.IllegalStateException This repeater is not in {@link State#STOPPED} or {@link State#FAILED} state.
     */
    @Override
    public synchronized final void run() {
        switch (state){
            case STOPPED:
            case FAILED:
                exception = null;
                //create thread worker
                final RepeaterWorker worker = new RepeaterWorker() {
                    @Override
                    public final void run() {
                        doAction();
                    }

                    @Override
                    public final void uncaughtException(final Thread t, final Throwable e) {
                        fault(e);
                    }
                };
                repeatThread = new RepeaterThreadImpl(worker, period);
                //executes periodic thread
                repeatThread.start();
                stateChanged(state = State.STARTED);
                return;
                default:
                    throw new IllegalStateException(String.format("The repeater must be in %s state but actual state is %s", State.STOPPED, state));

        }
    }

    /**
     * Stops the timer and blocks the current thread until pending the last executed action.
     * @param timeout Time to wait for pending of the last executed action.
     * @throws TimeoutException The last executed action is not completed in the specified time.
     * @throws InterruptedException The blocked thread is interrupted.
     */
    public final synchronized void stop(final TimeSpan timeout) throws TimeoutException, InterruptedException{
        switch (state){
            case STOPPING:
                repeatThread.join(timeout);
                return;
            case STARTED:
                repeatThread.interrupt();
                state = State.STOPPING;
                repeatThread.join(timeout);
                state = State.STOPPED;

                return;
            default:
                throw new IllegalStateException(String.format("The repeater must be in %s state but actual state is %s", State.STARTED, state));
        }
    }

    /**
     * Releases all resources associated with this repeater but not wait for
     * action completion.
     */
    @Override
    public final synchronized void close() {
        if(repeatThread != null) repeatThread.interrupt();
    }
}
