package com.bytex.snamp.moa;

import com.google.common.util.concurrent.AtomicDouble;

import javax.annotation.Nonnull;

/**
 * Abstract class for building exponentially weighted moving average.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class DoubleEMA extends EMA {
    private static final long serialVersionUID = 4963517746376695068L;
    private final AtomicDouble oldValue;

    DoubleEMA(){
        oldValue = new AtomicDouble(Double.NaN);
    }

    DoubleEMA(final DoubleEMA other){
        oldValue = new AtomicDouble(other.oldValue.get());
    }

    @Override
    @Nonnull
    public abstract DoubleEMA clone();

    abstract double getDecay();

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public final void accept(final double value) {
        final double alpha = getDecay();
        double oldValue, newValue;
        do {
            oldValue = this.oldValue.get();
            newValue = Double.isNaN(oldValue) ? value : oldValue + alpha * (value - oldValue);
        } while (!this.oldValue.compareAndSet(oldValue, newValue));
    }

    /**
     * Computes average value.
     *
     * @return Average value.
     */
    @Override
    public final double doubleValue() {
        final double result = oldValue.get();
        return Double.isNaN(result) ? 0D : result;
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public final void reset() {
        oldValue.set(Double.NaN);
    }
}
