package com.bytex.snamp.moa;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Abstract class for building exponentially weighted moving average.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class BigDecimalEMA extends Average implements Supplier<BigDecimal>, Consumer<BigDecimal> {
    private static final long serialVersionUID = 4963517746376695068L;
    private final AtomicReference<BigDecimal> oldValue;
    private final MathContext context;

    BigDecimalEMA(@Nonnull final MathContext context){
        oldValue = new AtomicReference<>();
        this.context = Objects.requireNonNull(context);
    }

    BigDecimalEMA(final BigDecimalEMA other){
        oldValue = new AtomicReference<>(other.oldValue.get());
        context = other.context;
    }

    abstract BigDecimal getDecay();

    @Override
    public final void accept(final BigDecimal value) {
        final BigDecimal alpha = getDecay();
        BigDecimal oldValue, newValue;
        do {
            oldValue = this.oldValue.get();
            newValue = oldValue == null ? value : value.subtract(oldValue).multiply(alpha).add(oldValue);
        } while (!this.oldValue.compareAndSet(oldValue, newValue));
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
}
