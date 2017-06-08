package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

/**
 * An exponentially-weighted moving average computed used {@link BigDecimal} at its core.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">Exponential moving average</a>
 */
public final class BigDecimalEMA extends AbstractEMA implements Consumer<BigDecimal>, DoubleConsumer, Serializable, Supplier<BigDecimal>, Stateful, Cloneable {
    private static final long serialVersionUID = -8885874345563930420L;

    private final AtomicReference<BigDecimal> adder;
    private final AtomicReference<BigDecimal> accumulator;
    private final BigDecimal alpha;
    private final MathContext context;
    private final BigDecimal precisionNanos;

    private BigDecimalEMA(final double intervalSeconds,
                                     final MathContext context,
                                     final Duration precision,
                                     final Duration measurementInterval){
        super(measurementInterval);
        adder = new AtomicReference<>(BigDecimal.ZERO);
        accumulator = new AtomicReference<>();
        alpha = BigDecimal.valueOf(1 - Math.exp(-measurementInterval.getSeconds() / intervalSeconds));
        this.context = Objects.requireNonNull(context);
        this.precisionNanos = BigDecimal.valueOf(precision.toNanos());
    }

    public BigDecimalEMA(final Duration interval,
                                    final MathContext context){
        this(interval.getSeconds(), context, DEFAULT_PRECISION, DEFAULT_INTERVAL);
    }

    private BigDecimalEMA(final BigDecimalEMA other) {
        super(other);
        adder = new AtomicReference<>(other.adder.get());
        accumulator = new AtomicReference<>(other.accumulator.get());
        alpha = other.alpha;
        context = other.context;
        precisionNanos = other.precisionNanos;
    }

    /**
     * Creates deep clone of this object.
     * @return Deep clone of this object.
     */
    public BigDecimalEMA clone() {
        return new BigDecimalEMA(this);
    }

    @Override
    public void reset() {
        super.reset();
        accumulator.set(null);
        adder.set(BigDecimal.ZERO);
    }

    private BigDecimal getAverage() {
        final BigDecimal instantCount = adder.getAndSet(BigDecimal.ZERO).divide(BigDecimal.valueOf(measurementIntervalNanos), context);
        if (accumulator.compareAndSet(null, instantCount)) //first time set
            return instantCount;
        else {
            BigDecimal next, prev;
            do {
                prev = accumulator.get();
                next = prev.add(alpha.multiply(instantCount.subtract(prev, context), context), context);
            } while (!accumulator.compareAndSet(prev, next));
            return next;
        }
    }

    @Override
    public void accept(final BigDecimal value) {
        BigDecimal next, prev;
        do {
            prev = adder.get();
            next = prev.add(value, context);
        } while (!adder.compareAndSet(prev, next));
    }

    @Override
    public void accept(double value) {
        accept(BigDecimal.valueOf(value));
    }

    @Override
    public BigDecimal get() {
        final long currentTime = System.nanoTime();
        final long startTime = getStartTime();
        final long age = currentTime - startTime;
        BigDecimal result = accumulator.get();
        if (age > measurementIntervalNanos) {
            final long newStartTime = currentTime - age % measurementIntervalNanos;
            if (setStartTime(startTime, newStartTime)) {
                for (int i = 0; i < age / measurementIntervalNanos; i++)
                    result = getAverage();
            }
        } else if (result == null)
            result = BigDecimal.ZERO;
        return result.multiply(precisionNanos, context);
    }

    @Override
    public int intValue() {
        return get().intValue();
    }

    @Override
    public long longValue() {
        return get().longValue();
    }

    @Override
    public float floatValue() {
        return get().floatValue();
    }

    @Override
    public double doubleValue() {
        return get().doubleValue();
    }

    @Override
    public byte byteValue() {
        return get().byteValue();
    }

    @Override
    public short shortValue() {
        return get().shortValue();
    }

    @Override
    public String toString() {
        return get().toString();
    }
}
