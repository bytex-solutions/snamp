package com.bytex.snamp.connector.metrics;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class RangedGaugeFPRecorder extends RangedValueFPRecorder implements RangedGaugeFP {
    private static final long serialVersionUID = 7803345140265603197L;
    private final GaugeFPRecorder gaugeFP;

    protected RangedGaugeFPRecorder(final RangedGaugeFPRecorder source) {
        super(source);
        gaugeFP = source.gaugeFP.clone();
    }

    public RangedGaugeFPRecorder(final String name, final double from, final double to) {
        super(name, from, to);
        gaugeFP = new GaugeFPRecorder(name);
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public void accept(final double value) {
        super.accept(value);
        gaugeFP.accept(value);
    }

    @Override
    public RangedGaugeFPRecorder clone() {
        return new RangedGaugeFPRecorder(this);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        super.reset();
        gaugeFP.reset();
    }

    @Override
    public final double getDeviation() {
        return gaugeFP.getDeviation();
    }

    @Override
    public final double getQuantile(final double quantile) {
        return gaugeFP.getQuantile(quantile);
    }

    @Override
    public final double getLastMeanValue(final MetricsInterval interval) {
        return gaugeFP.getLastMeanValue(interval);
    }

    /**
     * Gets maximum value ever presented.
     *
     * @return Maximum value ever presented.
     */
    @Override
    public final double getMaxValue() {
        return gaugeFP.getMaxValue();
    }

    /**
     * Gets maximum value for the last period.
     *
     * @param interval Period.
     * @return Maximum value of the last period.
     */
    @Override
    public final double getLastMaxValue(final MetricsInterval interval) {
        return gaugeFP.getLastMaxValue(interval);
    }

    /**
     * The minimum value ever presented.
     *
     * @return The minimum value ever presented.
     */
    @Override
    public final double getMinValue() {
        return gaugeFP.getMinValue();
    }

    /**
     * Gets minimum value for the last period.
     *
     * @param interval Period.
     * @return Minimum value for the last period.
     */
    @Override
    public final double getLastMinValue(final MetricsInterval interval) {
        return gaugeFP.getLastMinValue(interval);
    }

    /**
     * The last presented value.
     *
     * @return The last presented value.
     */
    @Override
    public final double getLastValue() {
        return gaugeFP.getLastValue();
    }
}
