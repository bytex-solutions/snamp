package com.bytex.snamp.connector.metrics;

/**
 * Abstract class for numeric gauges.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractNumericGauge extends AbstractMetric implements NumericGauge {

    AbstractNumericGauge(String name) {
        super(name);
    }
}
