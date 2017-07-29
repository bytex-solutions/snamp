package com.bytex.snamp.connector.metrics;

/**
 * Represents {@link Flag} that rates input stream of {@code boolean} values.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface RatedFlag extends Flag, Rate {
    @Override
    RatedFlag clone();
}
