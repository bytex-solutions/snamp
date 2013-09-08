package com.snamp;

import java.util.concurrent.TimeUnit;

/**
 * Represents timeout information.
 * @author roman
 */
public final class TimeSpan {
    /**
     * Represents infinite timeout.
     */
    public static final TimeSpan infinite = null;

    /**
     * Represents the time value.
     */
    public final int time;

    /**
     * Represents time measurement unit.
     */
    public final TimeUnit unit;

    /**
     * Initializes a new time span.
     * @param time The time value.
     * @param unit The time measurement unit.
     */
    public TimeSpan(final int time, final TimeUnit unit) {
      this.time = time;
      this.unit = unit == null ? TimeUnit.MILLISECONDS : unit;
    }

    /**
     * Initializes a new millisecond time span.
     * @param milliseconds The number of milliseconds,
     */
    public TimeSpan(final int milliseconds) {
        this(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the string representation of this instance.
     * @return The string representation of this instance.
     */
    @Override
    public String toString() {
        return String.format("%n %s", time, unit);
    }

    /**
     * Determines whether this instance represents the same time as the specified object.
     * @param obj An object to compare.
     * @return {@literal true}, if this object the same time as the specified object.
     */
    public boolean equals(final TimeSpan obj) {
        return obj != null && unit.toMillis(time) == obj.unit.toMillis(obj.time);
    }

    /**
     * Determines whether this instance represents the same time as the specified object.
     * @param obj An object to compare.
     * @return {@literal true}, if this object the same time as the specified object.
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TimeSpan && equals((TimeSpan)obj);
    }
}
