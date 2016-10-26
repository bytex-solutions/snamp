package com.bytex.snamp.connector.mda;

import com.bytex.snamp.ThreadSafe;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents simple thread-safe timer.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
final class SimpleTimer extends AtomicLong implements AccessTimer {
    private static final long serialVersionUID = -4969777929724121792L;

    /**
     * Initializes a new timer in the current point in time.
     */
    public SimpleTimer(){
        super(System.nanoTime());
    }

    /**
     * Resets this timer atomically.
     */
    public void reset(){
        set(System.nanoTime());
    }

    /**
     * Compares interval of time measured by this timer with the
     * specified interval, in nanoseconds.
     * @param nanos An interval to compare, in nanoseconds.
     * @return Interval comparison result:
     *      >0, if this timer represents interval greater than specified;
     *      <0, if this timer represents interval smaller than specified;
     *      =0, if this timer represents the same interval as specified.
     */
    public int checkInterval(final long nanos){
        return Long.compare(System.nanoTime() - get(), nanos);
    }

    /**
     * Compares interval of time measured by this timer with the
     * specified interval.
     * @param interval An interval to compare.
     * @param unit Unit of measurements of interval.
     * @return Interval comparison result:
     *      >0, if this timer represents interval greater than specified;
     *      <0, if this timer represents interval smaller than specified;
     *      =0, if this timer represents the same interval as specified.
     */
    public int checkInterval(final long interval, final TimeUnit unit){
        return checkInterval(unit.toNanos(interval));
    }

    /**
     * Compares interval of time measured by this timer with the
     * specified interval.
     * @param interval An interval to compare.
     * @return Interval comparison result:
     *      >0, if this timer represents interval greater than specified;
     *      <0, if this timer represents interval smaller than specified;
     *      =0, if this timer represents the same interval as specified.
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(final Duration interval) {
        return checkInterval(interval.toNanos(), TimeUnit.NANOSECONDS);
    }
}
