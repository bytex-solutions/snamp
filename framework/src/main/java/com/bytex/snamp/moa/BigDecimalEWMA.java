package com.bytex.snamp.moa;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Abstract class for building exponentially weighted moving average.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class BigDecimalEWMA extends EWMA implements BigDecimalAverage, UnaryOperator<BigDecimal> {
    private static final long serialVersionUID = 4963517746376695068L;
    private final AtomicReference<BigDecimal> oldValue;
    protected final MathContext context;

    protected BigDecimalEWMA(@Nonnull final MathContext context){
        oldValue = new AtomicReference<>();
        this.context = Objects.requireNonNull(context);
    }

    protected BigDecimalEWMA(final BigDecimalEWMA other){
        oldValue = new AtomicReference<>(other.oldValue.get());
        context = other.context;
    }

    protected abstract BigDecimal getDecay();

    @Override
    public final BigDecimal apply(final BigDecimal value) {
        final BigDecimal alpha = getDecay();
        BigDecimal oldValue, newValue;
        do {
            oldValue = this.oldValue.get();
            newValue = oldValue == null ? value : value.subtract(oldValue).multiply(alpha).add(oldValue);
        } while (!this.oldValue.compareAndSet(oldValue, newValue));
        return newValue;
    }

    @Override
    public final void accept(final BigDecimal value) {
        apply(value);
    }

    @Override
    public final void accept(final double value) {
        accept(new BigDecimal(value, context));
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public BigDecimal get() {
        return firstNonNull(oldValue.get(), BigDecimal.ZERO);
    }

    /**
     * Computes average value.
     *
     * @return Average value.
     */
    @Override
    public final double doubleValue() {
        return get().doubleValue();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void reset() {
        oldValue.set(null);
    }

    @Override
    public String toString() {
        return get().toString();
    }

    static BigDecimalEWMA fixedRate(final Duration meanLifetime,
                                    final Duration measurementInterval,
                                    final MathContext mathContext) {
        final class FixedIntervalBigDecimalEWMA extends BigDecimalEWMA {
            private static final long serialVersionUID = 6614789316030642885L;
            private final BigDecimal alpha;

            private FixedIntervalBigDecimalEWMA() {
                super(mathContext);
                alpha = new BigDecimal(computeAlpha(meanLifetime, measurementInterval), context);
            }

            private FixedIntervalBigDecimalEWMA(final FixedIntervalBigDecimalEWMA other) {
                super(other);
                alpha = other.alpha;
            }

            @Nonnull
            @Override
            public FixedIntervalBigDecimalEWMA clone() {
                return new FixedIntervalBigDecimalEWMA(this);
            }

            @Override
            protected BigDecimal getDecay() {
                return alpha;
            }
        }
        return new FixedIntervalBigDecimalEWMA();
    }

    public static BigDecimalEWMA floatingInterval(final Duration meanLifetime, final MathContext mathContext) {
        final class FloatingIntervalBigDecimalEWMA extends BigDecimalEWMA {
            private static final long serialVersionUID = -6087005535730908375L;
            private final FloatingDecay decay;

            private FloatingIntervalBigDecimalEWMA() {
                super(mathContext);
                decay = new FloatingDecay(meanLifetime);
            }

            private FloatingIntervalBigDecimalEWMA(final FloatingIntervalBigDecimalEWMA other) {
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
            public FloatingIntervalBigDecimalEWMA clone() {
                return new FloatingIntervalBigDecimalEWMA(this);
            }

            @Override
            protected BigDecimal getDecay() {
                return new BigDecimal(decay.getAsDouble(), context);
            }
        }
        return new FloatingIntervalBigDecimalEWMA();
    }
}
