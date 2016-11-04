package com.bytex.snamp.connector.md.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.notifications.NotificationBuilder;
import com.bytex.snamp.connector.notifications.measurement.InstantMeasurement;
import com.bytex.snamp.connector.notifications.measurement.SpanMeasurement;
import com.bytex.snamp.connector.notifications.measurement.StopwatchMeasurement;
import com.bytex.snamp.scripting.groovy.Scriptlet;

/**
 * Represents notification parser written in Groovy language.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyNotificationParser extends Scriptlet implements NotificationParser {
    /**
     * Constructs a new notification builder.
     * @return A new notification builder.
     */
    @SpecialUse
    protected static NotificationBuilder newNotification() {
        return new NotificationBuilder();
    }

    @SpecialUse
    protected static SpanMeasurement.Builder newSpan(){
        return SpanMeasurement.builder();
    }

    @SpecialUse
    protected static StopwatchMeasurement.Builder newStopwatch(){
        return StopwatchMeasurement.builder();
    }

    @SpecialUse
    protected static InstantMeasurement.BooleanMeasurementBuilder newInstantBoolean(){
        return InstantMeasurement.builderForBoolean();
    }

    @SpecialUse
    protected static InstantMeasurement.DoubleMeasurementBuilder newInstantDouble(){
        return InstantMeasurement.builderForDouble();
    }
}
