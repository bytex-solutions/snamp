package com.bytex.snamp.connector.mda.impl.measurement;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Gauge extends StatDataCollector {
    /**
     * Gets instant set values.
     * <p>
     *     Zero index in array represents the first dimension.
     * @return Instant set of values.
     */
    Comparable<?>[] measure();
}
