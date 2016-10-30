package com.bytex.snamp.connector.metrics;

import java.util.function.DoubleConsumer;

/**
 * Measures normative recorder for floating-point numbers with double precision.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class NormativeFPRecorder extends AbstractNormativeRecorder implements DoubleConsumer {
    private static final long serialVersionUID = -5078974473014037352L;
    private final double rangeStart;
    private final double rangeEnd;

    protected NormativeFPRecorder(final NormativeFPRecorder source) {
        super(source);
        rangeStart = source.rangeStart;
        rangeEnd = source.rangeEnd;
    }

    public NormativeFPRecorder(final String name, final double from, final double to) {
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
    public NormativeFPRecorder clone() {
        return new NormativeFPRecorder(this);
    }
}
