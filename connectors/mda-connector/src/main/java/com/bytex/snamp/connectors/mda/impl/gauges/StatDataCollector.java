package com.bytex.snamp.connectors.mda.impl.gauges;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface StatDataCollector extends Iterable<Axis<?>> {
    byte getAxles();

    /**
     * Gets information about axis.
     * @param dimension Zero-based index of axis: X - 0, Y - 1, Z - 2
     * @return Information about axis.
     */
    Axis<?> getAxis(final byte dimension);
}
