package com.bytex.snamp.concurrent;

import com.bytex.snamp.TimeSpan;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents synchronization primitive based on looping using
 * the custom condition.
 * @param <T> Type of the spinning result.
 * @param <E> Type of the exception that may be thrown by synchronization method.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class SpinWait<T, E extends Throwable> implements Awaitor<T, E> {
    private final TimeSpan delay;
    /**
     * Represents default value of the spin delay.
     */
    protected static final TimeSpan DEFAULT_SPIN_DELAY = new TimeSpan(1);

    protected SpinWait(final TimeSpan spinDelay){
        this.delay = spinDelay;
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
     * @throws E Internal checker error.
     */
    protected abstract T get() throws E;

    /**
     * Blocks the caller thread until the event will not be raised.
     *
     * @param timeout Event waiting timeout.
     * @return The event data.
     * @throws java.util.concurrent.TimeoutException timeout parameter too small for waiting.
     * @throws InterruptedException                  Waiting thread is aborted.
     * @throws E {@link #get()} throws an exception.
     * @see #get()
     */
    @Override
    public final T await(final TimeSpan timeout) throws TimeoutException, InterruptedException, E {
        if(timeout == null) return await();
        final Stopwatch timer = Stopwatch.createStarted();
        T result;
        while ((result = get()) == null)
            if(timer.elapsed(TimeUnit.MILLISECONDS) > timeout.toMillis()) throw new TimeoutException("Spin wait timed out");
            else if(Thread.interrupted()) throw spinWaitInterrupted();
            else if(delay != null) Thread.sleep(delay.toMillis());
        return result;
    }

    /**
     * Blocks the caller thread (may be infinitely) until the event will not be raised.
     *
     * @return The event data.
     * @throws InterruptedException Waiting thread is aborted.
     * @throws E {@link #get()} throws an exception.
     * @see #get()
     */
    @Override
    public final T await() throws InterruptedException, E {
        T result;
        while ((result = get()) == null)
            if(Thread.interrupted()) throw spinWaitInterrupted();
            else if(delay != null) Thread.sleep(delay.toMillis());
        return result;
    }

    private static InterruptedException spinWaitInterrupted(){
        return new InterruptedException("SpinWait interrupted");
    }
}
