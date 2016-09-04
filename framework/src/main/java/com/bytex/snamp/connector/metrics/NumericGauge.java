package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of numeric type.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface NumericGauge<V extends Number & Comparable<V>> extends Gauge<V>, Statistic {
}
