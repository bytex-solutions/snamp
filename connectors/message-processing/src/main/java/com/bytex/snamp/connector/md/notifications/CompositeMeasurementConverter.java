package com.bytex.snamp.connector.md.notifications;

import com.bytex.snamp.ClassMap;
import com.bytex.snamp.instrumentation.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class CompositeMeasurementConverter extends ClassMap<MeasurementConverter> implements MeasurementConverter<Measurement> {
    private static final long serialVersionUID = 7018253712732614291L;

    public final <M extends Measurement> CompositeMeasurementConverter addConverter(final Class<M> measurementType,
                                                                                    final MeasurementConverter<M> converter){
        put(measurementType, converter);
        return this;
    }

    public final CompositeMeasurementConverter addDefaultConverters(final Object source){
        return addConverter(Span.class, s -> new SpanNotification(source, s))
                .addConverter(TimeMeasurement.class, t -> new TimeMeasurementNotification(source, t))
                .addConverter(BooleanMeasurement.class, b -> new BooleanMeasurementNotification(source, b))
                .addConverter(IntegerMeasurement.class, i -> new IntegerMeasurementNotification(source, i))
                .addConverter(FloatingPointMeasurement.class, f -> new FloatingPointMeasurementNotification(source, f))
                .addConverter(StringMeasurement.class, s -> new StringMeasurementNotification(source, s));
    }

    /**
     * Converts {@link Measurement} into {@link MeasurementNotification}.
     *
     * @param measurement A measurement to convert.
     * @return Notification container
     */
    @Override
    public MeasurementNotification<Measurement> apply(final Measurement measurement) {
        return null;
    }
}
