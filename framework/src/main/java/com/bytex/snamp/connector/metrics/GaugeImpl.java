package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedObject;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents basic implementation of gauge.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
class GaugeImpl<V extends Comparable<V>> extends AbstractMetric implements Gauge<V>, Consumer<V>, BiConsumer<BinaryOperator<V>, V> {
    private final AtomicReference<V> maxValue;
    private final AtomicReference<V> minValue;
    private final AtomicReference<V> lastValue;
    private final MetricsIntervalMap<TimeLimitedObject<V>> lastMaxValues;
    private final MetricsIntervalMap<TimeLimitedObject<V>> lastMinValues;
    private final V initialValue;

    GaugeImpl(final String name,
              final V initialValue) {
        super(name);
        this.initialValue = Objects.requireNonNull(initialValue);
        maxValue = new AtomicReference<>();
        minValue = new AtomicReference<>();
        lastValue = new AtomicReference<>();
        lastMinValues = new MetricsIntervalMap<>(interval -> interval.createTemporaryBox(null, GaugeImpl::minValue));
        lastMaxValues = new MetricsIntervalMap<>(interval -> interval.createTemporaryBox(null, GaugeImpl::maxValue));
    }

    /**
     * Gets maximum value for the last period.
     *
     * @param interval Period.
     * @return Maximum value of the last period.
     */
    @Override
    public final V getLastMaxValue(final MetricsInterval interval) {
        return firstNonNull(lastMaxValues.get(interval, TimeLimitedObject::get), initialValue);
    }

    /**
     * Gets minimum value for the last period.
     *
     * @param interval Period.
     * @return Minimum value for the last period.
     */
    @Override
    public final V getLastMinValue(final MetricsInterval interval) {
        return firstNonNull(lastMinValues.get(interval, TimeLimitedObject::get), initialValue);
    }

    private static  <V extends Comparable<V>> V maxValue(final V current, final V provided){
        if(current == null)
            return provided;
        else
            return current.compareTo(provided) > 0 ? current : provided;
    }

    private static  <V extends Comparable<V>> V minValue(final V current, final V provided){
        if(current == null)
            return provided;
        else
            return current.compareTo(provided) < 0 ? current : provided;
    }

    protected void writeValue(final V value){
        maxValue.accumulateAndGet(value, GaugeImpl::maxValue);
        minValue.accumulateAndGet(value, GaugeImpl::minValue);
        lastMaxValues.forEachAccept(value, TimeLimitedObject::accept);
        lastMinValues.forEachAccept(value, TimeLimitedObject::accept);
    }

    public final void updateValue(final UnaryOperator<V> operator){
        V prev, next;
        do{
            next = operator.apply(prev = lastValue.get());
        } while (!lastValue.compareAndSet(prev, next));
        writeValue(next);
    }

    @Override
    public final void accept(final BinaryOperator<V> operator, final V value) {
        writeValue(lastValue.accumulateAndGet(value, operator));
    }

    @Override
    public final void accept(final V value) {
        lastValue.set(value);
        writeValue(value);
    }

    /**
     * Gets maximum value ever presented.
     *
     * @return The maximum value ever presented.
     */
    @Override
    public final V getMaxValue() {
        return firstNonNull(maxValue.get(), initialValue);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        maxValue.set(null);
        minValue.set(null);
        lastMaxValues.values().forEach(TimeLimitedObject::reset);
        lastMinValues.values().forEach(TimeLimitedObject::reset);
    }

    /**
     * The minimum value ever presented.
     *
     * @return The minimum value ever presented.
     */
    @Override
    public final V getMinValue() {
        return firstNonNull(minValue.get(), initialValue);
    }

    /**
     * The last presented value.
     *
     * @return The last presented value.
     */
    @Override
    public final V getLastValue() {
        return firstNonNull(lastValue.get(), initialValue);
    }
}
