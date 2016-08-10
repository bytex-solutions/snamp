package com.bytex.snamp.connector.mda;

import java.time.Duration;

/**
 * Represents timer that measures time of the last access.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface AccessTimer extends Comparable<Duration> {
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
    int compareTo(final Duration expirationTime);
}
