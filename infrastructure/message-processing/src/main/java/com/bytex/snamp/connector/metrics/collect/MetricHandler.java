package com.bytex.snamp.connector.metrics.collect;

import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a handler for a metric.
 */
interface MetricHandler extends Metric, Predicate<MeasurementNotification>, Consumer<MeasurementNotification> {
}
