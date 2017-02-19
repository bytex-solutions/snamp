package com.bytex.snamp.concurrent;

import com.bytex.snamp.MethodStub;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents lightweight timer that is used to repeat some action in time.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public abstract class Repeater implements AutoCloseable, Runnable {
    /**
     * Represents state of this timer.
     * @author Roman Sakno
     * @version 2.0
     * @since 1.0
     */
    public enum RepeaterState {
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
        CLOSED;
    }

    private static final AtomicLong THREAD_COUNTER = new AtomicLong(0L);
    private RepeaterState state;
    private Throwable exception;
    private final Duration period;
    private Thread repeatThread;
    private final Lock monitor;

    /**
     * Initializes a new repeater.
     * @param period Time between successive task executions. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException period is {@literal null}.
     */
    protected Repeater(final Duration period){
        this.state = RepeaterState.STOPPED;
        this.period = Objects.requireNonNull(period);
        this.exception = null;
        this.repeatThread = null;
        this.monitor = new ReentrantLock();
    }

    /**
     * Initializes a new repeater.
     * @param period Time between successive task executions, in millis.
     */
    protected Repeater(final long period) {
        this(Duration.ofMillis(period));
    }

    /**
     * Generates name of the repeater thread.
     * @return A new unique name of the repeater thread.
     */
    protected String generateThreadName(){
        return String.format("%sThread#%s", getClass().getSimpleName(), THREAD_COUNTER.getAndIncrement());
    }

    /**
     * Gets priority of the repeater thread.
     * @return Priority of the repeater thread.
     */
    protected int getPriority(){
        return Thread.NORM_PRIORITY;
    }

    /**
     * Returns time between successive task executions.
     * @return Time between successive task executions.
     */
    public Duration getPeriod(){
        return period;
    }

    /**
     * Returns the state of this repeater.
     * @return The state of this repeater.
     * @see RepeaterState
     */
    public final RepeaterState getState(){
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
    protected void stateChanged(final RepeaterState s){

    }

    /**
     * Provides some periodical action.
     * @throws Exception Action is failed.
     */
    protected abstract void doAction() throws Exception;

    private void runImpl() {
        final class RepeaterThread extends Thread implements Thread.UncaughtExceptionHandler, Callable<Void> {
            private RepeaterThread() {
                super(generateThreadName());
                setDaemon(true);
                setPriority(getPriority());
                setUncaughtExceptionHandler(this);
            }

            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                if (monitor.tryLock())    //to avoid deadlock
                    try {
                        switch (state) {
                            case STARTED:
                                final RepeaterState newState = e instanceof InterruptedException ?
                                        RepeaterState.STOPPED :
                                        RepeaterState.FAILED;
                                exception = e;
                                stateChanged(state = newState);
                                t.interrupt();
                                break;
                            default:
                                throw new IllegalStateException(String.format("The repeater must be in %s state but actual state is %s", RepeaterState.STARTED, state));
                        }
                    } finally {
                        monitor.unlock();
                    }
            }

            @Override
            public Void call() throws Exception {
                doAction();
                return null;
            }

            @Override
            public void run() {
                while (!isInterrupted()) {
                    //sleep for a specified time
                    try {
                        Thread.sleep(getPeriod().toMillis());
                    } catch (final InterruptedException e) {
                        return;
                    }
                    callUnchecked(this);
                }
            }

            @Override
            public String toString() {
                return getName();
            }
        }

        switch (state) {
            case STOPPED:
            case FAILED:
                exception = null;
                repeatThread = new RepeaterThread();
                //execute periodic thread
                repeatThread.start();
                stateChanged(state = RepeaterState.STARTED);
                return;
            default:
                throw new IllegalStateException(String.format("The repeater must be in %s state but actual state is %s", RepeaterState.STOPPED, state));

        }
    }

    /**
     * Executes the repeater.
     * @throws java.lang.IllegalStateException This repeater is not in {@link RepeaterState#STOPPED} or {@link RepeaterState#FAILED} state.
     */
    @Override
    public final void run() {
        monitor.lock();
        try{
            runImpl();
        }
        finally {
            monitor.unlock();
        }
    }

    private void tryStop(final long timeoutMillis) throws TimeoutException, InterruptedException {
        switch (state) {
            default:
                return;  //already stopped
            case STARTED:
                repeatThread.interrupt();
                stateChanged(state = RepeaterState.STOPPING);
            case STOPPING:
                if (timeoutMillis > 0L)
                    repeatThread.join(timeoutMillis);
                if (repeatThread.isAlive())
                    throw new TimeoutException(String.format("Thread %s still alive", repeatThread));
        }
        repeatThread = null;
        stateChanged(state = RepeaterState.STOPPED);
    }

    private void stop(final long millis) throws TimeoutException, InterruptedException {
        long duration = System.currentTimeMillis();
        if (monitor.tryLock(millis, TimeUnit.MILLISECONDS)) {
            duration -= System.currentTimeMillis();
            try {
                tryStop(millis - duration);
            } finally {
                monitor.unlock();
            }
        } else
            throw new TimeoutException("Too small timeout " + millis);
    }

    /**
     * Stops the timer and blocks the current thread until pending the last executed action.
     * @param timeout Time to wait for pending of the last executed action.
     * @throws TimeoutException The last executed action is not completed in the specified time.
     * @throws InterruptedException The blocked thread is interrupted.
     */
    public final void stop(final Duration timeout) throws TimeoutException, InterruptedException {
        stop(timeout.toMillis());
    }

    private void closeImpl() {
        exception = null;
        stateChanged(state = RepeaterState.CLOSED);
    }

    /**
     * Stops the timer, blocks the current thread until pending the last execution and
     * then releases all resources associated with this repeater.
     * @param timeout Time to wait for pending of the last executed action.
     * @throws TimeoutException The last executed action is not completed in the specified time.
     * @throws InterruptedException The blocked thread is interrupted.
     */
    public final void close(final Duration timeout) throws TimeoutException, InterruptedException{
        long duration = System.currentTimeMillis();
        if (monitor.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            duration = System.currentTimeMillis() - duration;
            try {
                tryStop(timeout.toMillis() - duration);
                closeImpl();    //must be closed before lock will be released
            } finally {
                monitor.unlock();
            }
        } else throw new TimeoutException("Too small timeout " + timeout);
    }

    /**
     * Releases all resources associated with this repeater but not wait for
     * action completion.
     */
    @Override
    public final void close() throws InterruptedException {
        monitor.lockInterruptibly();
        try {
            if (repeatThread != null)
                repeatThread.interrupt();
            closeImpl();
        } finally {
            monitor.unlock();
            repeatThread = null;
        }
    }
}
