package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.metrics.ArrivalsRecorder;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;

import java.time.Duration;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
final class SpanArrivalsRecorder extends ArrivalsRecorder {
    static final String NAME = "Arrivals";
    private static final long serialVersionUID = 4061075096831015800L;

    SpanArrivalsRecorder(final int samplingSize) {
        super(DataStreamConnector.ARRIVALS_METRIC, samplingSize);
    }

    private SpanArrivalsRecorder(final SpanArrivalsRecorder recorder) {
        super(recorder);
    }

    void accept(final SpanNotification notification) {
        accept(notification.getMeasurement().convertTo(Duration.class));
    }

    @Override
    public SpanArrivalsRecorder clone() {
        return new SpanArrivalsRecorder(this);
    }
}
