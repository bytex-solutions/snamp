package com.bytex.snamp.connector.http;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.connector.notifications.measurement.InstantMeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.instrumentation.BooleanMeasurement;
import com.bytex.snamp.instrumentation.Measurement;

import java.util.HashMap;

/**
 * Provides conversion between {@link Measurement} and {@link MeasurementNotification}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class MeasurementConverter extends HashMap<Class<? extends Measurement>, ToMeasurementNotificationFunction> {
    private static final long serialVersionUID = -608166073781369733L;
    private static final LazySoftReference<MeasurementConverter> INSTANCE = new LazySoftReference<>();

    private MeasurementConverter(){

    }

    private <M extends Measurement> MeasurementConverter registerConverter(final Class<M> measurementType, final ToMeasurementNotificationFunction<? super M> converter){
        put(measurementType, converter);
        return this;
    }

    private static InstantMeasurementNotification fromBooleanMeasurement(final BooleanMeasurement measurement){
        return InstantMeasurementNotification.builderForBoolean()
                .setSource(measurement.getComponentName(), measurement.getInstanceName())
                .setTimeStamp(measurement.getTimeStamp())
                .setMessage(measurement.getMessage())
                .setSequenceNumber(0L)
                .get();
    }

    private static MeasurementConverter createConverter(){
        return new MeasurementConverter()
                .registerConverter(BooleanMeasurement.class, MeasurementConverter::fromBooleanMeasurement);

    }

    private static MeasurementConverter getConverter(){
        return INSTANCE.lazyGet(MeasurementConverter::createConverter);
    }

    @SuppressWarnings("unchecked")
    static MeasurementNotification convert(final Measurement measurement){
        final ToMeasurementNotificationFunction converter = getConverter().get(measurement.getClass());
        return converter != null ? converter.apply(measurement) : null;
    }
}
