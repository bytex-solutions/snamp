package com.bytex.snamp.connector.md.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.notifications.*;
import com.bytex.snamp.connector.notifications.NotificationBuilder;
import com.bytex.snamp.instrumentation.*;
import com.bytex.snamp.scripting.groovy.Scriptlet;

import javax.management.Notification;
import java.util.Map;

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
    protected static Span newSpan(){
        return new Span();
    }

    @SpecialUse
    protected static TimeMeasurement newStopwatch(){
        return new TimeMeasurement();
    }

    @SpecialUse
    protected static BooleanMeasurement newInstantBoolean(){
        return new BooleanMeasurement();
    }

    @SpecialUse
    protected static FloatingPointMeasurement newInstantDouble(){
        return new FloatingPointMeasurement();
    }

    @SpecialUse
    protected static IntegerMeasurement newIntegerMeasurement(){
        return new IntegerMeasurement();
    }

    @SpecialUse
    protected static StringMeasurement newStringMeasurement(){
        return new StringMeasurement();
    }

    protected abstract Object parse(final Object headers, final Object body) throws Exception;

    @Override
    public final Notification parse(final Map<String, ?> headers, final Object body) throws Exception {
        final Object result = parse((Object) headers, body);
        if(result instanceof Span)
            return new SpanNotification(this, (Span) result);
        else if(result instanceof TimeMeasurement)
            return new TimeMeasurementNotification(this, (TimeMeasurement) result);
        else if(result instanceof IntegerMeasurement)
            return new IntegerMeasurementNotification(this, (IntegerMeasurement) result);
        else if(result instanceof FloatingPointMeasurement)
            return new FloatingPointMeasurementNotification(this, (FloatingPointMeasurement) result);
        else if(result instanceof BooleanMeasurement)
            return new BooleanMeasurementNotification(this, (BooleanMeasurement) result);
        else if(result instanceof StringMeasurement)
            return new StringMeasurementNotification(this, (StringMeasurement) result);
        else
            return null;
    }
}
