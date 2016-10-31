package com.bytex.snamp.connector.metrics;

import java.util.function.LongConsumer;

/**
 * Measures normative recorder for 64-bit signed integers.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class RangedValue64Recorder extends AbstractRangedRecorder implements LongConsumer {
    private static final long serialVersionUID = 2992622425510225162L;
    private final long rangeStart;
    private final long rangeEnd;

    protected RangedValue64Recorder(final RangedValue64Recorder source) {
        super(source);
        rangeStart = source.rangeStart;
        rangeEnd = source.rangeEnd;
    }

    public RangedValue64Recorder(final String name, final long from, final long to) {
        super(name);
        if(from > to)
            throw new IllegalArgumentException("Incorrect range");
        this.rangeStart = from;
        this.rangeEnd = to;
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public void accept(final long value) {
        updateValue(HitResult.compute(rangeStart, rangeEnd, value));
    }

    @Override
    public RangedValue64Recorder clone() {
        return new RangedValue64Recorder(this);
    }
}
