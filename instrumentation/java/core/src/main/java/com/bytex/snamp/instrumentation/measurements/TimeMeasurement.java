package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.TimeUnit;

/**
 * Represents measurement of time.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
@JsonTypeName("timeSpan")
public class TimeMeasurement extends Measurement {
    private static final long serialVersionUID = -6706383213005311530L;

    @JsonProperty("d")
    private long duration;

    @JsonProperty("u")
    @JsonSerialize(using = TimeUnitSerializer.class)
    @JsonDeserialize(using = TimeUnitDeserializer.class)
    private TimeUnit timeUnit;

    public TimeMeasurement(final long duration, final TimeUnit unit) {
        this.duration = duration;
        this.timeUnit = notNull(unit);
    }

    public TimeMeasurement(){
        this(0L, TimeUnit.NANOSECONDS);
    }

    private static TimeUnit notNull(final TimeUnit unit){
        if (unit == null)
            throw new IllegalArgumentException("unit cannot be null");
        return unit;
    }

    @JsonIgnore
    public final void setDuration(final long duration, final TimeUnit unit){
        this.duration = duration;
        this.timeUnit = notNull(unit);
    }

    public final double getDuration(final TimeUnit unit) {
        return unit.convert(duration, timeUnit);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeLong(duration);
        out.writeUTF(timeUnit.name());
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        duration = in.readLong();
        timeUnit = TimeUnit.valueOf(in.readUTF());
        super.readExternal(in);
    }

    @SuppressWarnings("Since15")
    private Object toDuration(){
        switch (timeUnit){
            case NANOSECONDS:
                return java.time.Duration.ofNanos(duration);
            case MICROSECONDS:
                return java.time.Duration.ofNanos(duration * 1000L);
            case MILLISECONDS:
                return java.time.Duration.ofMillis(duration);
            case SECONDS:
                return java.time.Duration.ofSeconds(duration);
            case MINUTES:
                return java.time.Duration.ofMinutes(duration);
            case HOURS:
                return java.time.Duration.ofHours(duration);
            case DAYS:
                return java.time.Duration.ofDays(duration);
            default:
                return java.time.Duration.ofNanos(timeUnit.toNanos(duration));//should never be happened
        }
    }

    private Object convertTo(final String className) throws IllegalArgumentException {
        switch (className) {
            case "java.time.Duration":
                return toDuration();
            case "java.lang.Long":
            case "long":
                return timeUnit.toNanos(duration);
            default:
                throw new IllegalArgumentException(String.format("Conversion to type %s is not supported", className));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T convertTo(final Class<T> type) throws IllegalArgumentException{
        return (T) convertTo(type.getName());
    }
}
