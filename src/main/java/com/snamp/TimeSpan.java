package com.snamp;

import java.util.Date;
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
     * Represents the duration value.
     */
    public final long duration;

    /**
     * Represents duration measurement unit.
     */
    public final TimeUnit unit;

    /**
     * Initializes a new duration span.
     * @param time The duration value.
     * @param unit The duration measurement unit.
     */
    public TimeSpan(final long time, final TimeUnit unit) {
      this.duration = time;
      this.unit = unit == null ? TimeUnit.MILLISECONDS : unit;
    }

    /**
     * Initializes a new millisecond duration span.
     * @param milliseconds The number of milliseconds,
     */
    public TimeSpan(final long milliseconds) {
        this(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Converts this span to another.
     * @param unit
     * @return
     */
    public TimeSpan convert(TimeUnit unit){
        if(unit == null) unit = TimeUnit.MILLISECONDS;
        return new TimeSpan(unit.convert(this.duration, this.unit));
    }

    /**
     * Returns the string representation of this instance.
     * @return The string representation of this instance.
     */
    @Override
    public String toString() {
        return String.format("%n %s", duration, unit);
    }

    /**
     * Determines whether this instance represents the same duration as the specified object.
     * @param obj An object to compare.
     * @return {@literal true}, if this object the same duration as the specified object.
     */
    public boolean equals(final TimeSpan obj) {
        return obj != null && unit.toMillis(duration) == obj.unit.toMillis(obj.duration);
    }

    /**
     * Determines whether this instance represents the same duration as the specified object.
     * @param obj An object to compare.
     * @return {@literal true}, if this object the same duration as the specified object.
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TimeSpan && equals((TimeSpan)obj);
    }

    /**
     * Computes difference between two dates.
     * @param left The left operand of the diff operation.
     * @param right The right operand of the diff operation.
     * @param unit The time measurement unit.
     * @return
     */
    public static TimeSpan diff(final Date left, final Date right, final TimeUnit unit){
        final TimeSpan temp = new TimeSpan(left.getTime() - right.getTime(), TimeUnit.MILLISECONDS);
        return temp.convert(unit);
    }
}
