package com.bytex.snamp.connector.metrics;

import java.util.function.DoubleConsumer;

/**
 * Measures normative recorder for floating-point numbers with double precision.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class RangedValueFPRecorder extends AbstractRangedRecorder implements DoubleConsumer {
    private static final long serialVersionUID = -5078974473014037352L;
    private final double rangeStart;
    private final double rangeEnd;

    protected RangedValueFPRecorder(final RangedValueFPRecorder source) {
        super(source);
        rangeStart = source.rangeStart;
        rangeEnd = source.rangeEnd;
    }

    public RangedValueFPRecorder(final String name, final double from, final double to) {
        super(name);
        rangeStart = from;
        rangeEnd = to;
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public void accept(final double value) {
        updateValue(HitResult.compute(rangeStart, rangeEnd, value));
    }

    @Override
    public RangedValueFPRecorder clone() {
        return new RangedValueFPRecorder(this);
    }
}
