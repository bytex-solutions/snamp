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
        super(MetricsInterval.class);
        for(final MetricsInterval interval: MetricsInterval.ALL_INTERVALS)
            put(interval, valueProvider.apply(interval));
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

    void acceptAsLong(final MetricsInterval interval, final long value, final ObjLongConsumer<V> consumer){
        consumer.accept(get(interval), value);
    }

    void acceptAsDouble(final MetricsInterval interval, final double value, final ObjDoubleConsumer<V> consumer){
        consumer.accept(get(interval), value);
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
