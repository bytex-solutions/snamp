package com.bytex.snamp.connector.md.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.notifications.NotificationBuilder;
import com.bytex.snamp.connector.notifications.measurement.InstantMeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.SpanMeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.StopwatchMeasurementNotification;
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
    protected static SpanMeasurementNotification.Builder newSpan(){
        return SpanMeasurementNotification.builder();
    }

    @SpecialUse
    protected static StopwatchMeasurementNotification.Builder newStopwatch(){
        return StopwatchMeasurementNotification.builder();
    }

    @SpecialUse
    protected static InstantMeasurementNotification.BooleanMeasurementBuilder newInstantBoolean(){
        return InstantMeasurementNotification.builderForBoolean();
    }

    @SpecialUse
    protected static InstantMeasurementNotification.DoubleMeasurementBuilder newInstantDouble(){
        return InstantMeasurementNotification.builderForDouble();
    }
}
