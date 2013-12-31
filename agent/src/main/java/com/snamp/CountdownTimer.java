package com.snamp;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents countdown timer that can be used to compute time intervals.
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
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    protected final void setTimerValue(final TimeSpan current) throws IllegalStateException{
        if(isStarted()) throw new IllegalStateException("The timer should be stopped.");
        elapsed = current != null ? current : new TimeSpan(Long.MAX_VALUE);
    }

    /**
     * Returns the elapsed time.
     * @return The elapsed time.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
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
    public final boolean start(){
        if(isEmpty()) return false;
        beginning = new Date();
        return true;
    }

    /**
     * Starts the timer.
     * @param timeoutException
     * @return {@literal true}, if timer started successfully; otherwise, {@literal false}.
     * @throws TimeoutException Attempts to start empty timer.
     */
    public final boolean start(final Activator<TimeoutException> timeoutException) throws TimeoutException{
        if(timeoutException == null) return start();
        else if(isEmpty()) throw timeoutException.newInstance();
        else return start();
    }

    /**
     * Composes {@link #stop()} and {@link #getElapsedTime()} methods.
     * @return The elapsed time.
     */
    public final TimeSpan stopAndGetElapsedTime(){
        stop();
        return getElapsedTime();
    }

    /**
     * Stops the timer.
     * @return {@literal}
     */
    public final boolean stop(){
        if(beginning == null) return false;
        elapsed = TimeSpan.diff(new Date(), beginning, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * Stops the timer.
     * @param timeoutException An activator that produces
     * @return {@literal true}, if timer is stopped successfully but not empty; otherwise, {@literal false}.
     * @throws TimeoutException The timer is empty.
     */
    public final boolean stop(final Activator<TimeoutException> timeoutException) throws TimeoutException{
        if(timeoutException == null) return stop();
        else if(stop())
            if(isEmpty()) throw timeoutException.newInstance();
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
