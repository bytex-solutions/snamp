/**
 * Represents a set of metrics.
 * Foundation metrics:
 * <ul>
 *     <li>{@link com.bytex.snamp.connector.metrics.Gauge64} for instant {@code long} values</li>
 *     <li>{@link com.bytex.snamp.connector.metrics.GaugeFP} for instant {@code double values}</li>
 *     <li>{@link com.bytex.snamp.connector.metrics.Flag} for instant {@code boolean} values</li>
 *     <li>{@link com.bytex.snamp.connector.metrics.StringGauge} for instant {@link java.lang.String} values</li>
 *     <li>{@link com.bytex.snamp.connector.metrics.Rate} for measurement of request rate</li>
 *     <li>{@link com.bytex.snamp.connector.metrics.Timer} for measurement of response time</li>
 * </ul>
 * Complex metrics:
 * <ul>
 *     <li>{@link com.bytex.snamp.connector.metrics.Arrivals} for measurement of arrivals in terms of Queuing Theory</li>
 *     <li>{@link com.bytex.snamp.connector.metrics.AttributeMetrics} represents usage statistics of resource attributes</li>
 *     <li>{@link com.bytex.snamp.connector.metrics.OperationMetric} represents usage statistics of resource operations</li>
 *     <li>{@link com.bytex.snamp.connector.metrics.NotificationMetric} represents usage statistics of resource notifications</li>
 * </ul>
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 * @see com.bytex.snamp.connector.metrics.MetricsSupport
 * @see com.bytex.snamp.connector.metrics.Metric
 */
package com.bytex.snamp.connector.metrics;