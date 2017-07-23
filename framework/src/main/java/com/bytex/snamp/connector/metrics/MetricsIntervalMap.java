package com.bytex.snamp.connector.metrics;

import java.util.EnumMap;
import java.util.function.*;

/**
 * Represents highly optimized immutable map with {@link MetricsInterval} keys.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class MetricsIntervalMap<V> extends EnumMap<MetricsInterval, V> {
    private static final long serialVersionUID = 1743299463619063796L;

    MetricsIntervalMap(final Function<? super MetricsInterval, ? extends V> valueProvider){
        this(MetricsInterval.ALL_INTERVALS, valueProvider);
    }

    MetricsIntervalMap(final Iterable<MetricsInterval> intervals, final Function<? super MetricsInterval, ? extends V> valueProvider){
        super(MetricsInterval.class);
        for(final MetricsInterval interval: intervals)
            put(interval, valueProvider.apply(interval));
    }

    MetricsIntervalMap(final MetricsIntervalMap<V> source, final UnaryOperator<V> cloneFn) {
        super(MetricsInterval.class);
        source.forEach((key, value) -> put(key, cloneFn.apply(value)));
    }

    double getAsDouble(final MetricsInterval interval, final ToDoubleFunction<? super V> converter){
        return converter.applyAsDouble(get(interval));
    }

    long getAsLong(final MetricsInterval interval, final ToLongFunction<? super V> converter){
        return converter.applyAsLong(get(interval));
    }

    <O> O get(final MetricsInterval interval, final Function<? super V, ? extends O> converter){
        return converter.apply(get(interval));
    }

    boolean acceptAsLong(final MetricsInterval interval, final long value, final ObjLongConsumer<V> consumer) {
        final boolean result;
        if (result = containsKey(interval))
            consumer.accept(get(interval), value);
        return result;
    }

    boolean acceptAsDouble(final MetricsInterval interval, final double value, final ObjDoubleConsumer<V> consumer) {
        final boolean result;
        if (result = containsKey(interval))
            consumer.accept(get(interval), value);
        return result;
    }

    void forEachAcceptLong(final long value, final ObjLongConsumer<V> consumer){
        for(final V v: values())
            consumer.accept(v, value);
    }

    void forEachAcceptDouble(final double value, final ObjDoubleConsumer<V> consumer){
        for(final V v: values())
            consumer.accept(v, value);
    }

    <I> void forEachAccept(final I value, final BiConsumer<V, I> consumer){
        for(final V v: values())
            consumer.accept(v, value);
    }
}
