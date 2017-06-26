package com.bytex.snamp.connector.metrics;

import javax.annotation.concurrent.ThreadSafe;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * Measures normative recorder for 64-bit signed integers.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public class RangedGauge64Recorder extends RangedValue64Recorder implements RangedGauge64 {
    private static final long serialVersionUID = -8084771859605410577L;
    private final Gauge64Recorder gauge64;

    protected RangedGauge64Recorder(final RangedGauge64Recorder source) {
        super(source);
        gauge64 = source.gauge64.clone();
    }

    public RangedGauge64Recorder(final String name, final long from, final long to) {
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

    public final long updateValue(final LongUnaryOperator operator) {
        final long result = gauge64.updateValue(operator);
        super.accept(result);
        return result;
    }

    public final long updateValue(final LongBinaryOperator operator, final long value) {
        final long result = gauge64.updateValue(operator, value);
        super.accept(result);
        return result;
    }

    @Override
    public RangedGauge64Recorder clone() {
        return new RangedGauge64Recorder(this);
    }

    @Override
    public final double getDeviation() {
        return gauge64.getDeviation();
    }

    @Override
    public final double getQuantile(final float quantile) {
        return gauge64.getQuantile(quantile);
    }

    @Override
    public final double getMeanValue(final MetricsInterval interval) {
        return gauge64.getMeanValue(interval);
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
