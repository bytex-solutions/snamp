package com.bytex.snamp.connector.md.groovy;

import com.bytex.snamp.ClassMap;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.notifications.*;
import com.bytex.snamp.connector.notifications.NotificationBuilder;
import com.bytex.snamp.instrumentation.*;
import com.bytex.snamp.scripting.groovy.Scriptlet;

import javax.management.Notification;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Represents notification parser written in Groovy language.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyNotificationParser extends Scriptlet implements NotificationParser {
    private static final String COMPONENT_NAME = "componentName";
    private static final String INSTANCE_NAME = "componentInstance";

    /**
     * Represents factory of {@link ValueMeasurement} as DSL element in Groovy.
     */
    protected final static class MeasurementBuilder{
        private MeasurementBuilder(){

        }

        @SpecialUse
        public <T extends ValueMeasurement> T of(final Supplier<? extends T> measurementFactory){
            return measurementFactory.get();
        }
    }

    @FunctionalInterface
    private interface ToNotificationFunction<M> extends BiFunction<Object, M, Notification> {
        @Override
        Notification apply(final Object source, final M message);
    }

    private static final class NotificationConverter extends ClassMap<ToNotificationFunction>{
        private static final long serialVersionUID = 5472344596553742321L;

        private <T> NotificationConverter registerConverter(final Class<T> type, final ToNotificationFunction<? super T> transformer){
            put(type, transformer);
            return this;
        }
    }

    private final NotificationConverter converter;

    /**
     * Initializes a new Groovy-based parser.
     */
    protected GroovyNotificationParser(){
        converter = new NotificationConverter()
                .registerConverter(Span.class, SpanNotification::new)
                .registerConverter(TimeMeasurement.class, TimeMeasurementNotification::new)
                .registerConverter(BooleanMeasurement.class, ValueMeasurementNotification::new)
                .registerConverter(IntegerMeasurement.class, ValueMeasurementNotification::new)
                .registerConverter(FloatingPointMeasurement.class, ValueMeasurementNotification::new)
                .registerConverter(StringMeasurement.class, ValueMeasurementNotification::new)
                .registerConverter(Notification.class, (source, notification) -> {
                    notification.setSource(source);
                    return notification;
                })
                .registerConverter(NotificationBuilder.class, (source, builder) -> {
                    builder.setSource(source);
                    return builder.get();
                });
    }

    //DSL keywords
    @SpecialUse
    protected static final Supplier<NotificationBuilder> notification = NotificationBuilder::new;
    @SpecialUse
    protected static final Supplier<Span> span = Span::new;
    @SpecialUse
    protected static final Supplier<TimeMeasurement> stopwatch = TimeMeasurement::new;
    @SpecialUse
    protected static final Supplier<MeasurementBuilder> measurement = MeasurementBuilder::new;
    //measurement types
    @SpecialUse
    protected static final Supplier<BooleanMeasurement> bool = BooleanMeasurement::new;
    @SpecialUse
    protected static final Supplier<IntegerMeasurement> integer = IntegerMeasurement::new;
    @SpecialUse
    protected static final Supplier<FloatingPointMeasurement> fp = FloatingPointMeasurement::new;
    @SpecialUse
    protected static final Supplier<StringMeasurement> str = StringMeasurement::new;

    /*
        DSL starter. Examples:
        create measurement of bool => create(measurement).of(bool)
        create notification => create(notification)
     */
    @SpecialUse
    protected static <T> T create(final Supplier<T> factory){
        return factory.get();
    }

    /**
     * This method should be overridden in Groovy script.
     * @param headers Headers to parse.
     * @param body Body to parse.
     * @return Notification; or measurement; or {@literal null}, if notification should be ignored.
     * @throws Exception Unable to
     */
    @SpecialUse
    protected abstract Object parse(final Object headers, final Object body) throws Exception;

    @SuppressWarnings("unchecked")
    @Override
    public final Notification parse(final Map<String, ?> headers, final Object body) throws Exception {
        final Object result = parse((Object) headers, body);
        if(result == null) return null;
        final ToNotificationFunction transformer = converter.getOrAdd(result.getClass());
        return transformer != null ? transformer.apply(this, result) : null;
    }

    public final void setComponentName(final String value){
        setProperty(COMPONENT_NAME, value);
    }

    public final void setInstanceName(final String value){
        setProperty(INSTANCE_NAME, value);
    }
}
