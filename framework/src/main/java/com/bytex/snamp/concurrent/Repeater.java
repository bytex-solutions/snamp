package com.bytex.snamp.concurrent;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.TimeSpan;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    public enum State{
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
        FAILED,

        /**
         * The timer is closed.
         */
        CLOSED,
    }

    private static final AtomicLong THREAD_COUNTER = new AtomicLong(0L);
    private volatile State state;
    private volatile Throwable exception;
    private final TimeSpan period;
    private RepeaterThread repeatThread;
    private final Lock monitor;

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
        this.monitor = new ReentrantLock();
    }

    /**
     * Initializes a new repeater.
     * @param period Time between successive task executions, in millis.
     */
    protected Repeater(final long period){
        this(new TimeSpan(period));
    }

    /**
     * Generates name of the repeater thread.
     * @return A new unique name of the repeater thread.
     */
    protected String generateThreadName(){
        return String.format("%sThread#%s", getClass().getSimpleName(), THREAD_COUNTER.getAndIncrement());
    }

    /**
     * Returns time between successive task executions.
     * @return Time between successive task executions.
     */
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
    @MethodStub
    protected void stateChanged(final State s){

    }

    @ThreadSafe(false)
    private void faultImpl(final Throwable e){
        switch (state) {
            case STARTED:
                if (repeatThread != null && repeatThread.getId() == Thread.currentThread().getId()) {
                    exception = e;
                    stateChanged(state = State.FAILED);
                    repeatThread.interrupt();
                    repeatThread = null;
                } else throw new IllegalStateException("This method should be called from the timer action.");
                break;
            default:
                throw new IllegalStateException(String.format("The repeater must be in %s state but actual state is %s", State.STARTED, state));
        }
    }


    /**
     * Informs this repeater about unhandled exception in the repeatable action.
     * <p>
     *     This method can be called only from timer thread (from {@link #doAction() method}.
     * </p>
     * @param e An exception occurred in the repeatable action.
     * @throws java.lang.IllegalStateException This repeater is not in {@link State#STARTED} state;
     *  or this method is not called from the {@link #doAction()} method.
     */
    @ThreadSafe
    protected final void fault(final Throwable e){
        monitor.lock();
        try {
            faultImpl(e);
        }
        finally {
            monitor.unlock();
        }
    }

    /**
     * Provides some periodical action.
     */
    protected abstract void doAction();

    private interface RepeaterWorker extends Runnable, Thread.UncaughtExceptionHandler{

    }

    private interface RepeaterThread extends Runnable{
        void start();
        void interrupt();
        long getId();
        void join(final long timeout) throws InterruptedException;
        boolean isAlive();
    }

    private final static class RepeaterThreadImpl extends Thread implements RepeaterThread{
        private final long period;

        private RepeaterThreadImpl(final RepeaterWorker worker,
                                   final String threadName,
                                   final TimeSpan period){
            super(worker, threadName);
            this.period = period.convert(TimeUnit.MILLISECONDS).duration;
            setDaemon(true);
            setUncaughtExceptionHandler(worker);
        }

        @Override
        public final void run() {
            while (!isInterrupted()){
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
        public String toString() {
            return getName();
        }
    }

    @ThreadSafe(false)
    private void runImpl(){
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
                repeatThread = new RepeaterThreadImpl(worker, generateThreadName(), period);
                //executes periodic thread
                repeatThread.start();
                stateChanged(state = State.STARTED);
                return;
            default:
                throw new IllegalStateException(String.format("The repeater must be in %s state but actual state is %s", State.STOPPED, state));

        }
    }

    /**
     * Executes the repeater.
     * @throws java.lang.IllegalStateException This repeater is not in {@link State#STOPPED} or {@link State#FAILED} state.
     */
    @Override
    @ThreadSafe
    public final void run() {
        monitor.lock();
        try{
            runImpl();
        }
        finally {
            monitor.unlock();
        }
    }

    private static void join(final RepeaterThread th, final long timeout) throws InterruptedException, TimeoutException {
        if (timeout > 0L) th.join(timeout);
        if (th.isAlive())
            throw new TimeoutException(String.format("Thread %s is alive", th));
    }

    @ThreadSafe(false)
    private boolean tryStop(long timeout) throws TimeoutException, InterruptedException{
        switch (state) {
            case STOPPING:
                join(repeatThread, timeout);
                break;
            case STARTED:
                repeatThread.interrupt();
                state = State.STOPPING;
                join(repeatThread, timeout);
                break;
            default:
                return false;
        }
        state = State.STOPPED;
        repeatThread = null;
        return true;
    }

    @ThreadSafe
    private void stop(long millis) throws TimeoutException, InterruptedException {
        long duration = System.currentTimeMillis();
        if (monitor.tryLock(millis, TimeUnit.MILLISECONDS)) {
            duration -= System.currentTimeMillis();
            millis -= duration;
            final boolean stopped;
            try {
                stopped = tryStop(millis);
            } finally {
                monitor.unlock();
            }
            if (!stopped)
                throw new IllegalStateException(String.format("The repeater must be in %s state but actual state is %s", State.STARTED, state));
        } else throw new TimeoutException("Too small timeout " + millis);
    }

    /**
     * Stops the timer and blocks the current thread until pending the last executed action.
     * @param timeout Time to wait for pending of the last executed action.
     * @throws TimeoutException The last executed action is not completed in the specified time.
     * @throws InterruptedException The blocked thread is interrupted.
     */
    public final void stop(final TimeSpan timeout) throws TimeoutException, InterruptedException {
        stop(timeout.toMillis());
    }

    private void closeImpl(){
        if(repeatThread != null) repeatThread.interrupt();
        state = State.CLOSED;
        repeatThread = null;
    }

    private void close(long millis) throws InterruptedException, TimeoutException {
        long duration = System.currentTimeMillis();
        if (monitor.tryLock(millis, TimeUnit.MILLISECONDS)) {
            duration -= System.currentTimeMillis();
            millis -= duration;
            try {
                tryStop(millis);
                closeImpl();    //must be closed before lock will be released
            } finally {
                monitor.unlock();
            }
        } else throw new TimeoutException("Too small timeout " + millis);
    }

    /**
     * Stops the timer, blocks the current thread until pending the last execution and
     * then releases all resources associated with this repeater.
     * @param timeout Time to wait for pending of the last executed action.
     * @throws TimeoutException The last executed action is not completed in the specified time.
     * @throws InterruptedException The blocked thread is interrupted.
     */
    public final void close(final TimeSpan timeout) throws TimeoutException, InterruptedException{
        close(timeout.toMillis());
    }

    /**
     * Releases all resources associated with this repeater but not wait for
     * action completion.
     */
    @Override
    public final void close() throws InterruptedException{
        monitor.lockInterruptibly();
        try{
            closeImpl();
        }
        finally {
            monitor.unlock();
        }
    }
}
