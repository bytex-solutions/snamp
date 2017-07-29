package com.bytex.snamp.moa;

import com.google.common.util.concurrent.AtomicDouble;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * Represents simple average computation service based on {@code double} data type at its core.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class SimpleDoubleAverage extends SimpleAverage {
    private static final long serialVersionUID = -7033204722759946995L;
    private final AtomicDouble summary;

    public SimpleDoubleAverage(final Duration ttl) {
        super(ttl);
        summary = new AtomicDouble(0D);
    }

    private SimpleDoubleAverage(final SimpleDoubleAverage other){
        super(other);
        summary = new AtomicDouble(other.summary.get());
    }

    @Override
    @Nonnull
    public SimpleDoubleAverage clone() {
        return new SimpleDoubleAverage(this);
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public void accept(final double value) {
        mark();
        summary.addAndGet(value);
    }

    /**
     * Computes average value.
     * @return Average value.
     */
    @Override
    public double doubleValue(){
        return getAverage(summary);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        summary.set(0D);
        super.reset();
    }
}
