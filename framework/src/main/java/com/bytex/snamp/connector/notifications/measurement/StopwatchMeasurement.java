package com.bytex.snamp.connector.notifications.measurement;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a notification of stopwatch measurement.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class StopwatchMeasurement extends Measurement {
    public static final String TYPE = "com.bytex.measurement.stopwatch";

    /**
     * Represents builder for {@link StopwatchMeasurement}.
     */
    public static class Builder extends MeasurementBuilder<StopwatchMeasurement>{
        private Duration duration;

        Builder(){
            duration = Duration.ZERO;
        }

        public final Builder setDuration(final Duration value){
            duration = Objects.requireNonNull(value);
            return this;
        }

        public final Builder setDuration(final long millis){
            return setDuration(Duration.ofMillis(millis));
        }

        protected final Duration getDuration(){
            return duration;
        }

        /**
         * Gets type of notification.
         *
         * @return Type of notification.
         */
        @Override
        public String getType() {
            return TYPE;
        }

        /**
         * Gets a result.
         *
         * @return a result
         */
        @Override
        public StopwatchMeasurement get() {
            final StopwatchMeasurement result = new StopwatchMeasurement(getSource(), getMessage());
            result.setTimeStamp(getTimeStamp());
            result.setUserData(getUserData());
            result.setDuration(duration);
            result.setSequenceNumber(getSequenceNumber(true));
            return result;
        }
    }

    private static final long serialVersionUID = -511699973106291280L;
    private Duration duration;

    StopwatchMeasurement(final String type, final Object source, final String message) {
        super(type, source, message);
        duration = Duration.ZERO;
        setTimeStamp(System.currentTimeMillis());
    }

    private StopwatchMeasurement(final Object source, final String message){
        this(TYPE, source, message);
    }

    /**
     * Constructs a new builder for {@link StopwatchMeasurement}.
     * @return A new builder.
     */
    public static Builder builder(){
        return new Builder();
    }

    /**
     * Set the notification timestamp.
     * @param value The notification timestamp. It indicates when the notification was generated.
     */
    public final void setTimeStamp(final Instant value){
        setTimeStamp(value.toEpochMilli());
    }

    /**
     * Gets duration of this span.
     * @return The duration of this span.
     */
    public final Duration getDuration(){
        return duration;
    }

    /**
     * Sets duration of this span.
     * @param value A new duration of this span.
     */
    public final void setDuration(final Duration value){
        this.duration = Objects.requireNonNull(value);
    }

    /**
     * Gets start time of this span.
     * @return The start time of this span.
     */
    public final Instant getStartTime() {
        return Instant.ofEpochMilli(Duration.ofMillis(getTimeStamp()).minus(duration).toMillis());
    }
}
