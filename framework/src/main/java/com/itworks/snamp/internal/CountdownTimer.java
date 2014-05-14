package com.itworks.snamp.internal;

import com.itworks.snamp.internal.semantics.Internal;
import com.itworks.snamp.internal.semantics.ThreadSafe;
import org.apache.commons.collections4.Factory;
import com.itworks.snamp.TimeSpan;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents countdown timer that can be used to compute time intervals.
 * <p>
 *     The precision of this countdown timer is 1 milliseconds. Any time intervals
 *     between {@link #start()} and {@link #stop()} invocations that less that 1 ms will
 *     not be recognized by timer.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public class CountdownTimer {
    private TimeSpan elapsed;
    private Date beginning;

    /**
     * Initializes a new countdown timer.
     * @param initial The initial timer value.
     * @throws IllegalArgumentException initial is null.
     */
    public CountdownTimer(final TimeSpan initial){
        this.elapsed = initial != null ? initial : new TimeSpan(Long.MAX_VALUE);
        this.beginning = null;
    }

    /**
     * Starts a new countdown timer.
     * @param initial The initial timer value.
     * @return A new instance of the started countdown timer.
     */
    public static CountdownTimer start(final TimeSpan initial){
        final CountdownTimer timer = new CountdownTimer(initial);
        timer.start();
        return timer;
    }

    /**
     * Sets the current timer value.
     * @param current The new value for the timer.
     * @throws IllegalStateException The timer should be stopped.
     */
    protected final void setTimerValue(final TimeSpan current) throws IllegalStateException{
        if(isStarted()) throw new IllegalStateException("The timer should be stopped.");
        elapsed = current != null ? current : new TimeSpan(Long.MAX_VALUE);
    }

    /**
     * Returns the elapsed time.
     * @return The elapsed time.
     */
    @ThreadSafe
    public final TimeSpan getElapsedTime(){
        return elapsed;
    }

    /**
     * Determines whether the timer is launched.
     * @return The timer is launched.
     */
    public final boolean isStarted(){
        return beginning != null;
    }

    /**
     * Determines whether the timer is empty.
     * @return The timer is empty.
     */
    public final boolean isEmpty(){
        return elapsed.duration <= 0L;
    }

    /**
     * Starts the timer.
     * @return {@literal true}, if timer is started successfully; otherwise, {@literal false}.
     */
    @ThreadSafe(false)
    public final boolean start(){
        if(isEmpty()) return false;
        beginning = new Date();
        return true;
    }

    /**
     * Starts the timer.
     * @param timeoutException A factory that produces a new instance of the exception
     *                         that can be raised when this timer is empty.
     * @return {@literal true}, if timer started successfully; otherwise, {@literal false}.
     * @throws TimeoutException Attempts to start empty timer.
     */
    @ThreadSafe(false)
    public final boolean start(final Factory<TimeoutException> timeoutException) throws TimeoutException{
        if(timeoutException == null) return start();
        else if(isEmpty()) throw timeoutException.create();
        else return start();
    }

    /**
     * Composes {@link #stop()} and {@link #getElapsedTime()} methods.
     * @return The elapsed time.
     */
    @ThreadSafe(false)
    public final TimeSpan stopAndGetElapsedTime(){
        stop();
        return getElapsedTime();
    }

    /**
     * Stops the timer.
     * @return {@literal}
     */
    @ThreadSafe(false)
    public final boolean stop(){
        if(beginning == null) return false;
        elapsed = TimeSpan.diff(new Date(), beginning, TimeUnit.MILLISECONDS);
        beginning = null;
        return true;
    }

    /**
     * Stops the timer.
     * @param timeoutException An activator that produces
     * @return {@literal true}, if timer is stopped successfully but not empty; otherwise, {@literal false}.
     * @throws TimeoutException The timer is empty.
     */
    @ThreadSafe(false)
    public final boolean stop(final Factory<TimeoutException> timeoutException) throws TimeoutException{
        if(timeoutException == null) return stop();
        else if(stop())
            if(isEmpty()) throw timeoutException.create();
            else return true;
        else return false;
    }

    /**
     * Determines whether the current timer contains the same elapsed time as other.
     * @param timer The timer to compare.
     * @return {@literal true}, if the specified object equals to this timer; otherwise, {@literal false}.
     */
    public final boolean equals(final CountdownTimer timer){
        return timer!=null && elapsed.equals(timer.elapsed);
    }

    /**
     * Determines whether the current timer contains the same elapsed time as other.
     * @param timer The timer to compare.
     * @return {@literal true}, if the specified object equals to this timer; otherwise, {@literal false}.
     */
    @Override
    public final boolean equals(final Object timer){
        return timer instanceof CountdownTimer && equals((CountdownTimer)timer);
    }
}
