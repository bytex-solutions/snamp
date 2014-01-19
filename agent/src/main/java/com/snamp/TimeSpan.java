package com.snamp;

import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents a time interval. This class cannot be inherited.<br/>
 *
 * <p><b>Example:</b><br/>
 * <pre>{@code
 *     TimeSpan t = new TimeSpan(10, TimeUnit.SECONDS); //represents 10 seconds
 *     System.out.println(t.duration); //10
 *     t = t.convert(TimeUnit.MILLISECONDS); //represents 10 second but in MILLISECONDS representation
 *     System.out.println(t.duration); //10000
 * }</pre>
 * </p>
 * <p>
 *     Note that the precision of this time interval is 1 nanosecond.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class TimeSpan {
    /**
     * Represents infinite time interval.
     */
    public static final TimeSpan INFINITE = null;

    /**
     * Represents the duration value.
     */
    public final long duration;

    /**
     * Represents duration measurement unit.
     */
    public final TimeUnit unit;

    /**
     * Initializes a new time interval.
     * @param time The duration value.
     * @param unit The duration measurement unit.
     */
    public TimeSpan(final long time, final TimeUnit unit) {
      this.duration = time;
      this.unit = unit == null ? TimeUnit.MILLISECONDS : unit;
    }

    /**
     * Initializes a new milliseconds interval.<br/>
     * <p>
     *     This constructor is equivalent to {@code new TimeSpan(value, TimeUnit.MILLISECONDS}</code>
     * </p>
     * @param milliseconds The number of milliseconds in interval.
     */
    public TimeSpan(final long milliseconds) {
        this(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Converts this span to another.
     * @param unit A new time interval measurement unit.
     * @return A new time interval with the specified measurement unit.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public TimeSpan convert(TimeUnit unit){
        if(unit == null) unit = TimeUnit.MILLISECONDS;
        return new TimeSpan(unit.convert(this.duration, this.unit));
    }

    /**
     * Increases up the scale of this time span.<br/>
     * <p>
     *     <b>Example:</b><br/>
     *     <pre>{@code
     *         TimeSpan value = new TimeSpan(1000);//1000 milliseconds<br/>
     *         System.out.println(value.duration);//1000<br/>
     *         value = value.up();<br/>
     *         System.out.println(value.duration); //1<br/>
     *     }</pre>
     * </p>
     * @return A newly created time span with increased scale,
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final TimeSpan up(){
        switch (this.unit){
            case NANOSECONDS: return convert(TimeUnit.MICROSECONDS);
            case MICROSECONDS: return convert(TimeUnit.MILLISECONDS);
            case MILLISECONDS: return convert(TimeUnit.SECONDS);
            case SECONDS: return convert(TimeUnit.MINUTES);
            case MINUTES: return convert(TimeUnit.HOURS);
            case HOURS: return convert(TimeUnit.DAYS);
            default: return this;
        }
    }

    /**
     * Returns the auto-scaled time interval.<br/>
     * <p>
     *     <b>Example:</b><br/>
     *     <pre>{@code
     *         TimeSpan value = new TimeSpan(60000);<br/>
     *         value = value.autoScale();//new time span has duration value = 1 and unit = MINUTES<br/>
     *     }</pre>
     * </p>
     * @return The auto-scaled time interval.
     * @see #autoScale(long, java.util.concurrent.TimeUnit)
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final TimeSpan autoScale(){
        TimeSpan result = this;
        while (result.duration > 0 && result.unit != TimeUnit.DAYS)
            result = result.up();
        return result;
    }

    /**
     * Creates a new auto-scaled time interval.
     * @param duration The interval value.
     * @param unit The interval measurement unit.
     * @return Auto-scaled time interval.
     * @see #autoScale()
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public static TimeSpan autoScale(final long duration, final TimeUnit unit){
        final TimeSpan temp = new TimeSpan(duration, unit);
        return temp.autoScale();
    }

    /**
     * Returns the string representation of this instance.
     * @return The string representation of this instance.
     */
    @Override
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final String toString() {
        return String.format("%s %s", duration, unit);
    }

    /**
     * Determines whether this instance represents the same duration as the specified object.
     * @param obj An object to compare.
     * @return {@literal true}, if this object the same duration as the specified object.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public boolean equals(final TimeSpan obj) {
        return obj != null && unit.toMillis(duration) == obj.unit.toMillis(obj.duration);
    }

    /**
     * Determines whether this instance represents the same duration as the specified object.
     * @param obj An object to compare.
     * @return {@literal true}, if this object the same duration as the specified object.
     */
    @Override
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public boolean equals(final Object obj) {
        return obj instanceof TimeSpan && equals((TimeSpan)obj);
    }

    /**
     * Computes difference between two dates.
     * @param left The left operand of the diff operation.
     * @param right The right operand of the diff operation.
     * @param unit The time measurement unit.
     * @return The difference between two dates.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public static TimeSpan diff(final Date left, final Date right, final TimeUnit unit){
        final TimeSpan temp = new TimeSpan(left.getTime() - right.getTime(), TimeUnit.MILLISECONDS);
        return temp.convert(unit);
    }
}
