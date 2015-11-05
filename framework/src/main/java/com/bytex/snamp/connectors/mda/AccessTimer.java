package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.TimeSpan;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AccessTimer extends Comparable<TimeSpan> {
    /**
     * Resets last access time.
     */
    void reset();

    /**
     * Compares interval of time measured by this timer with the
     * specified interval, in nanoseconds.
     * @param expirationTime An interval to compare.
     * @return Interval comparison result:
     *      >0, if this timer represents interval greater than specified;
     *      <0, if this timer represents interval smaller than specified;
     *      =0, if this timer represents the same interval as specified.
     */
    @Override
    int compareTo(final TimeSpan expirationTime);
}
