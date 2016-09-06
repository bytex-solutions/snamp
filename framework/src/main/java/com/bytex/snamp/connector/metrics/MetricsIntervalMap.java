package com.bytex.snamp.connector.metrics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import static java.util.AbstractMap.SimpleImmutableEntry;

/**
 * Represents highly optimized immutable map with {@link MetricsInterval} keys.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class MetricsIntervalMap<V> implements Map<MetricsInterval, V> {
    private static final ImmutableSet<MetricsInterval> KEYS = ImmutableSet.copyOf(MetricsInterval.ALL_INTERVALS);

    private final V secondValue;
    private final V minuteValue;
    private final V fiveMinutesValue;
    private final V fifteenMinutesValue;
    private final V hourValue;
    private final V twelveHoursValue;
    private final V dayValue;

    MetricsIntervalMap(final Function<? super MetricsInterval, ? extends V> valueProvider){
        secondValue = valueProvider.apply(MetricsInterval.SECOND);
        minuteValue = valueProvider.apply(MetricsInterval.MINUTE);
        fiveMinutesValue = valueProvider.apply(MetricsInterval.FIVE_MINUTES);
        fifteenMinutesValue = valueProvider.apply(MetricsInterval.FIFTEEN_MINUTES);
        hourValue = valueProvider.apply(MetricsInterval.HOUR);
        twelveHoursValue = valueProvider.apply(MetricsInterval.TWELVE_HOURS);
        dayValue = valueProvider.apply(MetricsInterval.DAY);
        assert KEYS.size() == 7;    //self-protection for early detection of situation when somehow add a new element in enum
    }

    void applyToAllIntervals(final Consumer<? super V> action){
        action.accept(secondValue);
        action.accept(minuteValue);
        action.accept(fiveMinutesValue);
        action.accept(fifteenMinutesValue);
        action.accept(hourValue);
        action.accept(twelveHoursValue);
        action.accept(dayValue);
    }

    @Override
    public int size() {
        return KEYS.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(final Object key) {
        return key instanceof MetricsInterval;
    }

    @Override
    public boolean containsValue(final Object value) {
        return values().equals(value);
    }

    V get(final MetricsInterval interval){
        switch (interval){
            case SECOND:
                return secondValue;
            case MINUTE:
                return minuteValue;
            case FIVE_MINUTES:
                return fiveMinutesValue;
            case FIFTEEN_MINUTES:
                return fifteenMinutesValue;
            case HOUR:
                return hourValue;
            case TWELVE_HOURS:
                return twelveHoursValue;
            case DAY:
                return dayValue;
            default:
                throw new IllegalArgumentException("Unexpected key " + interval);
        }
    }

    @Override
    public V get(final Object key) {
        return key instanceof MetricsInterval ? get((MetricsInterval) key) : null;
    }

    @Override
    public V put(final MetricsInterval key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends MetricsInterval, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableSet<MetricsInterval> keySet() {
        return KEYS;
    }

    @Override
    public ImmutableList<V> values() {
        return ImmutableList.of(secondValue, minuteValue);
    }


    private Entry<MetricsInterval, V> newEntry(final MetricsInterval key){
        return new SimpleImmutableEntry<>(key, get(key));
    }

    @Override
    public Set<Entry<MetricsInterval, V>> entrySet() {
        return keySet().stream()
                .map(this::newEntry)
                .collect(Collectors.toSet());
    }
}
