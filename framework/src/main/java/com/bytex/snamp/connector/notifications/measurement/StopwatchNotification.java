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
public class StopwatchNotification extends MeasurementNotification {
    private static final long serialVersionUID = -511699973106291280L;
    private Duration duration;

    public static final String TYPE = "com.bytex.measurement.stopwatch";

    StopwatchNotification(final String type, final String componentName, final String instanceName, final String message) {
        super(type, componentName, instanceName, message);
        duration = Duration.ZERO;
        setTimeStamp(System.currentTimeMillis());
    }

    public StopwatchNotification(final String componentName, final String instanceName, final String message){
        this(TYPE, componentName, instanceName, message);
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
