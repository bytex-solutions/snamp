package com.bytex.snamp.connector.metrics;

/**
 * Measures normative recorder for 64-bit signed integers.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class Gauge64WithNormativeRecorder extends Normative64Recorder implements Gauge64WithNormative {
    private static final long serialVersionUID = -8084771859605410577L;
    private final Gauge64Recorder gauge64;

    protected Gauge64WithNormativeRecorder(final Gauge64WithNormativeRecorder source) {
        super(source);
        gauge64 = source.gauge64.clone();
    }

    public Gauge64WithNormativeRecorder(final String name, final long from, final long to) {
        super(name, from, to);
        gauge64 = new Gauge64Recorder(name);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        super.reset();
        gauge64.reset();
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public void accept(final long value) {
        super.accept(value);
        gauge64.accept(value);
    }

    @Override
    public Gauge64WithNormativeRecorder clone() {
        return new Gauge64WithNormativeRecorder(this);
    }

    @Override
    public final double getDeviation() {
        return gauge64.getDeviation();
    }

    @Override
    public final double getQuantile(final double quantile) {
        return gauge64.getQuantile(quantile);
    }

    @Override
    public final double getLastMeanValue(final MetricsInterval interval) {
        return gauge64.getLastMeanValue(interval);
    }

    /**
     * Gets maximum value ever presented.
     *
     * @return Maximum value ever presented.
     */
    @Override
    public final long getMaxValue() {
        return gauge64.getMaxValue();
    }

    /**
     * Gets maximum value for the last period.
     *
     * @param interval Period.
     * @return Maximum value of the last period.
     */
    @Override
    public final long getLastMaxValue(final MetricsInterval interval) {
        return gauge64.getLastMaxValue(interval);
    }

    /**
     * The minimum value ever presented.
     *
     * @return The minimum value ever presented.
     */
    @Override
    public final long getMinValue() {
        return gauge64.getMinValue();
    }

    /**
     * Gets minimum value for the last period.
     *
     * @param interval Period.
     * @return Minimum value for the last period.
     */
    @Override
    public final long getLastMinValue(final MetricsInterval interval) {
        return gauge64.getLastMinValue(interval);
    }

    /**
     * The last presented value.
     *
     * @return The last presented value.
     */
    @Override
    public final long getLastValue() {
        return gauge64.getLastValue();
    }
}
