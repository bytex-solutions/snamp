package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
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
public final class BigDecimalEWMA extends EWMA implements Consumer<BigDecimal>, DoubleConsumer, Serializable, Supplier<BigDecimal>, Stateful, Cloneable {
    private static final long serialVersionUID = -8885874345563930420L;

    private final AtomicReference<BigDecimal> adder;
    private final AtomicReference<BigDecimal> accumulator;
    private final MathContext context;
    private final BigDecimal precisionNanos;
    private final BigDecimal alpha;
    private final BigDecimal measurementIntervalNanos;

    private BigDecimalEWMA(final Duration meanLifetime,
                           final MathContext context,
                           final Duration precision,
                           final Duration measurementInterval) {
        super(meanLifetime, measurementInterval);
        adder = new AtomicReference<>(BigDecimal.ZERO);
        alpha = new BigDecimal(super.alpha, context);
        accumulator = new AtomicReference<>();
        this.context = Objects.requireNonNull(context);
        precisionNanos = BigDecimal.valueOf(precision.toNanos());
        measurementIntervalNanos = new BigDecimal(super.measurementIntervalNanos, context);
    }

    public BigDecimalEWMA(final Duration meanLifetime,
                          final MathContext context){
        this(meanLifetime, context, DEFAULT_PRECISION, DEFAULT_INTERVAL);
    }

    public BigDecimalEWMA(final long meanLifetime, final TemporalUnit unit, final MathContext context){
        this(Duration.of(meanLifetime, unit), context);
    }

    private BigDecimalEWMA(final BigDecimalEWMA other) {
        super(other);
        adder = new AtomicReference<>(other.adder.get());
        accumulator = new AtomicReference<>(other.accumulator.get());
        context = other.context;
        precisionNanos = other.precisionNanos;
        alpha = other.alpha;
        measurementIntervalNanos = other.measurementIntervalNanos;
    }

    /**
     * Creates deep clone of this object.
     * @return Deep clone of this object.
     */
    public BigDecimalEWMA clone() {
        return new BigDecimalEWMA(this);
    }

    @Override
    public void reset() {
        super.reset();
        accumulator.set(null);
        adder.set(BigDecimal.ZERO);
    }

    private BigDecimal getAverage() {
        final BigDecimal instantCount = adder.getAndSet(BigDecimal.ZERO).divide(measurementIntervalNanos, context);
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
        if (age > super.measurementIntervalNanos) {
            final long newStartTime = currentTime - age % super.measurementIntervalNanos;
            if (setStartTime(startTime, newStartTime)) {
                for (int i = 0; i < age / super.measurementIntervalNanos; i++)
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
