package com.bytex.snamp;

import com.google.common.primitives.Longs;

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents a time interval. This class cannot be inherited.<br/>
 *
 * <p><b>Example:</b><br/>
 * <pre>{@code
 *     TimeSpan t = TimeSpan.ofSeconds(10); //represents 10 seconds
 *     System.out.println(t.duration); //10
 *     t = t.convert(TimeUnit.MILLISECONDS); //represents 10 second but in MILLISECONDS representation
 *     System.out.println(t.duration); //10000
 * }</pre>
 * </p>
 * <p>
 *     Note that the precision of this time interval is 1 nanosecond.
 * </p>
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 * @see <a href="http://openjdk.java.net/jeps/169">Java Value Object</a>
 * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html">Value-based class</a>
 */
public final class TimeSpan implements Serializable, Comparable<TimeSpan> {
    /**
     * Represents infinite time interval.
     */
    public static final TimeSpan INFINITE = null;

    /**
     * Represents empty time span.
     */
    public static final TimeSpan ZERO = new TimeSpan(0L, TimeUnit.NANOSECONDS);

    private static final long serialVersionUID = -5363358862646385345L;

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
    private TimeSpan(final long time, final TimeUnit unit) {
      this.duration = time;
      this.unit = unit == null ? TimeUnit.MILLISECONDS : unit;
    }

    /**
     * Creates a new time span from {@link Duration}.
     * @param value {@link Duration} to convert.
     * @return A new time span.
     * @since 1.2
     */
    public static TimeSpan of(final Duration value){
        return new TimeSpan(value.toNanos(), TimeUnit.NANOSECONDS);
    }


    public static TimeSpan of(final long time, final TimeUnit unit){
        return new TimeSpan(time, unit);
    }

    /**
     * Creates a new time span from the nanoseconds.
     * @param nanos Time span, in nanoseconds.
     * @return A new time span.
     */
    public static TimeSpan ofNanos(final long nanos){
        return of(nanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Creates a new time span from the nanoseconds.
     * @param nanos Time span, in nanoseconds.
     * @return A new time span.
     */
    public static TimeSpan ofNanos(final String nanos){
        return ofNanos(Long.parseLong(nanos));
    }

    /**
     * Creates a new time span from the milliseconds.
     * @param millis Time span, in milliseconds.
     * @return A new time span.
     */
    public static TimeSpan ofMillis(final long millis){
        return of(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new time span from the milliseconds.
     * @param millis Time span, in milliseconds.
     * @return A new time span.
     */
    public static TimeSpan ofMillis(final String millis){
        return ofMillis(Long.parseLong(millis));
    }

    /**
     * Creates a new time span from the microseconds.
     * @param micros Time span, in microseconds.
     * @return A new time span.
     */
    public static TimeSpan ofMicros(final long micros){
        return of(micros, TimeUnit.MICROSECONDS);
    }

    /**
     * Creates a new time span from the microseconds.
     * @param micros Time span, in microseconds.
     * @return A new time span.
     */
    public static TimeSpan ofMicros(final String micros){
        return ofMicros(Long.parseLong(micros));
    }

    /**
     * Creates a new time span from the seconds.
     * @param seconds Time span, in seconds.
     * @return A new time span.
     */
    public static TimeSpan ofSeconds(final long seconds){
        return of(seconds, TimeUnit.SECONDS);
    }

    /**
     * Creates a new time span from the seconds.
     * @param seconds Time span, in seconds.
     * @return A new time span.
     */
    public static TimeSpan ofSeconds(final String seconds){
        return ofSeconds(Long.parseLong(seconds));
    }

    /**
     * Creates a new time span from the minutes.
     * @param minutes Time span, in minutes.
     * @return A new time span.
     */
    public static TimeSpan ofMinutes(final long minutes){
        return of(minutes, TimeUnit.MINUTES);
    }

    /**
     * Creates a new time span from the minutes.
     * @param minutes Time span, in minutes.
     * @return A new time span.
     */
    public static TimeSpan ofMinutes(final String minutes){
        return ofMinutes(Long.parseLong(minutes));
    }

    /**
     * Creates a new time span from the hours.
     * @param hours Time span, in hours.
     * @return A new time span.
     */
    public static TimeSpan ofHours(final long hours){
        return of(hours, TimeUnit.HOURS);
    }

    /**
     * Creates a new time span from the hours.
     * @param hours Time span, in hours.
     * @return A new time span.
     */
    public static TimeSpan ofHours(final String hours){
        return ofHours(Long.parseLong(hours));
    }

    /**
     * Creates a new time span from the days.
     * @param days Time span, in days.
     * @return A new time span.
     */
    public static TimeSpan ofDays(final long days){
        return of(days, TimeUnit.DAYS);
    }

    /**
     * Creates a new time span from the days.
     * @param days Time span, in days.
     * @return A new time span.
     */
    public static TimeSpan ofDays(final String days){
        return ofDays(Long.parseLong(days));
    }

    /**
     * Converts this span to another.
     * @param unit A new time interval measurement unit.
     * @return A new time interval with the specified measurement unit.
     */
    @ThreadSafe
    public TimeSpan convert(final TimeUnit unit) {
        return unit == null ? convert(TimeUnit.MILLISECONDS) : new TimeSpan(unit.convert(this.duration, this.unit), unit);
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
    @ThreadSafe
    public TimeSpan up(){
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
     * Returns the string representation of this instance.
     * @return The string representation of this instance.
     */
    @Override
    @ThreadSafe
    public String toString() {
        return duration + " " + unit;
    }

    private boolean equals(final TimeSpan obj) {
        if(obj == null) return false;
        else if(unit.equals(obj.unit)) return duration == obj.duration;
        else return unit.toNanos(duration) == obj.unit.toNanos(obj.duration);
    }

    /**
     * Determines whether this instance represents the same duration as the specified object.
     * @param obj An object to compare.
     * @return {@literal true}, if this object the same duration as the specified object.
     */
    @Override
    @ThreadSafe
    public boolean equals(final Object obj) {
        return obj instanceof TimeSpan && equals((TimeSpan)obj);
    }

    /**
     * Computes hash code for this object.
     * @return A hash code for this object.
     */
    @Override
    public int hashCode() {
        return Longs.hashCode(toNanos());
    }

    /**
     * Computes difference between two dates.
     * @param left The left operand of the diff operation.
     * @param right The right operand of the diff operation.
     * @param unit The time measurement unit.
     * @return The difference between two dates.
     */
    @ThreadSafe
    public static TimeSpan diff(final Date left, final Date right, final TimeUnit unit){
        final TimeSpan temp = new TimeSpan(left.getTime() - right.getTime(), TimeUnit.NANOSECONDS);
        return temp.convert(unit);
    }

    public TimeSpan subtract(final TimeSpan value){
        if(value == null)
            return null;
        else if(unit == value.unit){
            final long dur = duration - value.duration;
            return dur <= 0L ? ZERO : new TimeSpan(dur, unit);
        }
        else {
            final long dur = toNanos() - value.toNanos();
            return dur <= 0L ? ZERO : new TimeSpan(dur, TimeUnit.NANOSECONDS);
        }
    }

    public TimeSpan subtract(final long duration, final TimeUnit unit) {
        return subtract(new TimeSpan(duration, unit));
    }

    public TimeSpan add(final TimeSpan value){
        if(value == INFINITE)
            return INFINITE;
        else if(unit == value.unit)
            return new TimeSpan(duration + value.duration, unit);
        else return new TimeSpan(toNanos() + value.toNanos(), TimeUnit.NANOSECONDS);
    }

    public TimeSpan add(final long duration, final TimeUnit unit){
        return add(new TimeSpan(duration, unit));
    }

    public long toNanos(){
        return unit.toNanos(duration);
    }

    public long toMillis(){
        return unit.toMillis(duration);
    }

    /**
     * Compares this time span with the specified time span.
     * @param other Time-based amount of time.
     * @return Comparison result.
     */
    @Override
    public int compareTo(final TimeSpan other) {
        if(other == INFINITE)
            return -1;
        else if(unit.equals(other.unit)) return Long.compare(duration, other.duration);
        else return Long.compare(toNanos(), other.toNanos());
    }

    private TemporalUnit getTemporalUnit(){
        switch (unit){
            case DAYS: return ChronoUnit.DAYS;
            case HOURS: return ChronoUnit.HOURS;
            case MICROSECONDS: return ChronoUnit.MICROS;
            case MILLISECONDS: return ChronoUnit.MILLIS;
            case MINUTES: return ChronoUnit.MINUTES;
            case NANOSECONDS: return ChronoUnit.NANOS;
            case SECONDS: return ChronoUnit.SECONDS;
            default: throw new IllegalStateException(String.format("Cannot convert %s to TemporalUnit", unit));
        }
    }

    /**
     * Converts this time span to {@link Duration}.
     * @return Time span as {@link Duration}.
     * @since 1.2
     */
    public Duration toDuration(){
        return Duration.of(duration, getTemporalUnit());
    }
}
