package com.snamp;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents countdown timer that can be used to compute timeout.
 * @author roman
 */
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
     * Returns elapsed time.
     * @return
     */
    public final TimeSpan getElapsedTime(){
        return elapsed;
    }

    /**
     * Determines whether the timer is launched.
     * @return
     */
    public final boolean isStarted(){
        return beginning != null;
    }

    /**
     * Determines whether the timer is empty.
     * @return
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
     * @return
     * @throws TimeoutException
     */
    public final boolean start(final Activator<TimeoutException> timeoutException) throws TimeoutException{
        if(timeoutException == null) return start();
        else if(isEmpty()) throw timeoutException.newInstance();
        else return start();
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
     * @param timer
     * @return
     */
    public final boolean equals(final CountdownTimer timer){
        return timer!=null && elapsed.equals(timer.elapsed);
    }

    /**
     * Determines whether the current timer contains the same elapsed time as other.
     * @param obj
     * @return
     */
    @Override
    public final boolean equals(final Object obj){
        return obj instanceof CountdownTimer && equals((CountdownTimer)obj);
    }
}
