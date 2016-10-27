package com.bytex.snamp.connector.metrics;

/**
 * Represents {@link GaugeFP} that rates input stream of {@code double} values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface RatedGaugeFP extends GaugeFP, Rate {
    @Override
    RatedGaugeFP clone();
}
