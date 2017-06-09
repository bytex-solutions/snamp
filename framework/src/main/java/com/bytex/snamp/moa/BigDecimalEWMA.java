package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.concurrent.Timeout;
import com.google.common.util.concurrent.AtomicDouble;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
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

    private static final class IntervalMemory extends Timeout implements Supplier<BigDecimal> {
        private static final long serialVersionUID = -3405864345563930421L;
        private final AtomicReference<BigDecimal> memory;

        private IntervalMemory(final Duration ttl) {
            super(ttl);
            memory = new AtomicReference<>(BigDecimal.ZERO);
        }

        private IntervalMemory(final IntervalMemory other) {
            super(other.getTimeout());
            memory = new AtomicReference<>(other.memory.get());
        }

        BigDecimal swap(final BigDecimal value) {
            if(resetIfExpired())
                return memory.getAndSet(value);
            else{
                memory.set(value);
                return BigDecimal.ZERO;
            }
        }

        @Override
        public void reset() {
            super.reset();
            memory.set(BigDecimal.ZERO);
        }

        @Override
        public BigDecimal get() {
            super.reset();
            return memory.getAndSet(BigDecimal.ZERO);
        }
    }

    private final AtomicReference<BigDecimal> adder;
    private final AtomicReference<BigDecimal> accumulator;
    private final MathContext context;
    private final BigDecimal precisionNanos;
    private final BigDecimal alpha;
    private final BigDecimal measurementIntervalNanos;
    private final IntervalMemory memory;

    public BigDecimalEWMA(final Duration meanLifetime,
                           final Duration measurementInterval,
                           final Precision precision,
                           final MathContext context) {
        super(meanLifetime, measurementInterval, precision);
        adder = new AtomicReference<>(BigDecimal.ZERO);
        alpha = new BigDecimal(super.alpha, context);
        accumulator = new AtomicReference<>();
        this.context = Objects.requireNonNull(context);
        precisionNanos = BigDecimal.valueOf(super.precisionNanos);
        measurementIntervalNanos = new BigDecimal(super.measurementIntervalNanos, context);
        memory = new IntervalMemory(measurementInterval);
    }

    /**
     * Initializes a new average calculator with measurement interval of 1 second.
     * @param meanLifetime Interval of time, over which the reading is said to be averaged. Cannot be {@literal null}.
     * @param context Settings which describe certain rules for numerical operators, such as those implemented by the {@link BigDecimal} class.
     */
    public BigDecimalEWMA(final Duration meanLifetime,
                          final MathContext context) {
        this(meanLifetime, Duration.ofSeconds(1), Precision.SECOND, context);
    }

    private BigDecimalEWMA(final BigDecimalEWMA other) {
        super(other);
        adder = new AtomicReference<>(other.adder.get());
        accumulator = new AtomicReference<>(other.accumulator.get());
        context = other.context;
        precisionNanos = other.precisionNanos;
        alpha = other.alpha;
        measurementIntervalNanos = other.measurementIntervalNanos;
        memory = new IntervalMemory(other.memory);
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
        final BigDecimal instantCount = adder
                .getAndSet(BigDecimal.ZERO)
                .add(memory.get())
                .divide(measurementIntervalNanos, context);
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

    public void append(final BigDecimal value){
        BigDecimal next, prev;
        do {
            prev = adder.get();
            next = prev.add(value, context);
        } while (!adder.compareAndSet(prev, next));
    }

    @Override
    public void append(final double value) {
        append(new BigDecimal(value, context));
    }

    @Override
    public void accept(final BigDecimal value) {
        append(memory.swap(value));
    }

    @Override
    public void accept(final double value) {
        accept(new BigDecimal(value, context));
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
