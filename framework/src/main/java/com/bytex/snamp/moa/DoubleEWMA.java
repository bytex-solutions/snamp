package com.bytex.snamp.moa;

import com.google.common.util.concurrent.AtomicDouble;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.util.function.DoubleUnaryOperator;

/**
 * Abstract class for building exponentially weighted moving average.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class DoubleEWMA extends EWMA implements DoubleUnaryOperator {
    private static final long serialVersionUID = 4963517746376695068L;
    private final AtomicDouble oldValue;

    protected DoubleEWMA(){
        oldValue = new AtomicDouble(Double.NaN);
    }

    protected DoubleEWMA(final DoubleEWMA other){
        oldValue = new AtomicDouble(other.oldValue.get());
    }

    @Override
    @Nonnull
    public abstract DoubleEWMA clone();

    protected abstract double getDecay();
    
    @Override
    public final double applyAsDouble(final double value) {
        final double alpha = getDecay();
        double oldValue, newValue;
        do {
            oldValue = this.oldValue.get();
            newValue = Double.isNaN(oldValue) ? value : oldValue + alpha * (value - oldValue);
        } while (!this.oldValue.compareAndSet(oldValue, newValue));
        return newValue;
    }

    @Override
    public final void accept(final double value) {
        applyAsDouble(value);
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
    @OverridingMethodsMustInvokeSuper
    public void reset() {
        oldValue.set(Double.NaN);
    }

    public static DoubleEWMA fixedInterval(final Duration meanLifetime,
                                    final Duration measurementInterval) {
        final class FixedIntervalDoubleEWMA extends DoubleEWMA {
            private static final long serialVersionUID = 6614789316030642885L;
            private final double alpha;

            private FixedIntervalDoubleEWMA() {
                alpha = computeAlpha(meanLifetime, measurementInterval);
            }

            private FixedIntervalDoubleEWMA(final FixedIntervalDoubleEWMA other) {
                alpha = other.alpha;
            }

            @Nonnull
            @Override
            public FixedIntervalDoubleEWMA clone() {
                return new FixedIntervalDoubleEWMA(this);
            }

            @Override
            protected double getDecay() {
                return alpha;
            }
        }
        return new FixedIntervalDoubleEWMA();
    }

    public static DoubleEWMA floatingInterval(final Duration meanLifetime) {
        final class FloatingIntervalDoubleEWMA extends DoubleEWMA {
            private static final long serialVersionUID = -6087005535730908375L;
            private final FloatingDecay decay;

            private FloatingIntervalDoubleEWMA() {
                decay = new FloatingDecay(meanLifetime);
            }

            private FloatingIntervalDoubleEWMA(final FloatingIntervalDoubleEWMA other) {
                super(other);
                decay = other.decay.clone();
            }

            @Override
            public void reset() {
                super.reset();
                decay.reset();
            }

            @Nonnull
            @Override
            public FloatingIntervalDoubleEWMA clone() {
                return new FloatingIntervalDoubleEWMA(this);
            }

            @Override
            protected double getDecay() {
                return decay.getAsDouble();
            }
        }
        return new FloatingIntervalDoubleEWMA();
    }
}
