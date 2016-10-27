package com.bytex.snamp.connector.metrics;

/**
 * Represents combination of {@link Timing} and {@link Rate}.
 * @since 2.0
 * @version 2.0
 */
public interface RatedTimer extends Rate, Timing {
    @Override
    RatedTimer clone();
}
