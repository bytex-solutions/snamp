package com.bytex.snamp.moa;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents simple average computation service based on {@link BigDecimal} data type at its core.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SimpleBigDecimalAverage extends SimpleAverage implements BigDecimalAverage {
    private static final long serialVersionUID = -1466595831850798503L;
    private final AtomicReference<BigDecimal> summary;
    private final MathContext context;

    public SimpleBigDecimalAverage(final Duration timeout, final MathContext context) {
        super(timeout);
        summary = new AtomicReference<>(BigDecimal.ZERO);
        this.context = Objects.requireNonNull(context);
    }

    private SimpleBigDecimalAverage(final SimpleBigDecimalAverage other) {
        super(other);
        summary = new AtomicReference<>(other.summary.get());
        context = other.context;
    }

    /**
     * Computes average value.
     *
     * @return Average value.
     */
    @Override
    public double doubleValue() {
        return get().doubleValue();
    }

    @Override
    @Nonnull
    public SimpleBigDecimalAverage clone() {
        return new SimpleBigDecimalAverage(this);
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public void accept(final double value) {
        accept(new BigDecimal(value, context));
    }

    @Override
    public void accept(final BigDecimal value) {
        mark();
        summary.accumulateAndGet(value, BigDecimal::add);
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public BigDecimal get() {
        return getAverage(summary, context);
    }
}
