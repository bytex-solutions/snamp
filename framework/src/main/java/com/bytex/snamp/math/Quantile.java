package com.bytex.snamp.math;

/**
 * Provides computation of quantile in a set of values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Quantile {
    private final int size;
    private final double[] values;

    public Quantile(final int samplingSize){
        this.size = samplingSize;
        this.values = new double[samplingSize * 2];
    }
}
