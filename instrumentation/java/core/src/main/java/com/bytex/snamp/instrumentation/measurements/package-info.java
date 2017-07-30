/**
 * Contains a set of Java classes representing atomic measurements:
 * <ul>
 *     <li>{@link com.bytex.snamp.instrumentation.measurements.ValueMeasurement} as a base
 *     class for all instant measurements of some scalar metrics such as RAM, CPU load etc.</li>
 *     <li>{@link com.bytex.snamp.instrumentation.measurements.TimeMeasurement} for time measurements such as response time and method execution time</li>
 *     <li>{@link com.bytex.snamp.instrumentation.measurements.Span} for internal and external spans</li>
 * </ul>
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 * @see com.bytex.snamp.instrumentation.measurements.Measurement
 */
package com.bytex.snamp.instrumentation.measurements;