package com.bytex.snamp;

import com.bytex.snamp.concurrent.LazyContainers;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Map.Entry;

/**
 * Represents functional primitive used in Java streams to split linear collection to the set of key/values pairs.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public final class KeyValueExtractor<I, K, V> implements Function<I, Entry<K, V>> {
    private static final class LazyEntry<I, K, V> implements Entry<K, V>{
        private final Supplier<K> key;
        private final Supplier<V> value;

        private LazyEntry(final I input, final Function<? super I, ? extends K> keyExtractor, final Function<? super I, ? extends V> valueExtractor){
            this.key = LazyContainers.THREAD_UNSAFE.of(() -> keyExtractor.apply(input));
            this.value = LazyContainers.THREAD_UNSAFE.of(() -> valueExtractor.apply(input));
        }

        @Override
        public K getKey() {
            return key.get();
        }

        @Override
        public V getValue() {
            return value.get();
        }

        @Override
        public V setValue(final V value) {
            throw new UnsupportedOperationException();
        }
    }

    private final Function<? super I, ? extends K> keyExtractor;
    private final Function<? super I, ? extends V> valueExtractor;

    private KeyValueExtractor(final Function<? super I, ? extends K> keyExtractor, final Function<? super I, ? extends V> valueExtractor){
        this.keyExtractor = Objects.requireNonNull(keyExtractor);
        this.valueExtractor = Objects.requireNonNull(valueExtractor);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param i the function argument
     * @return the function result
     */
    @Override
    public Entry<K, V> apply(final I i) {
        return new LazyEntry<>(i, keyExtractor, valueExtractor);
    }

    public static <I, K, V> KeyValueExtractor<I, K, V> of(final Function<? super I, ? extends K> keyExtractor, final Function<? super I, ? extends V> valueExtractor){
        return new KeyValueExtractor<>(keyExtractor, valueExtractor);
    }

    public static <I, V> KeyValueExtractor<I, I, V> of(final Function<? super I, ? extends V> valueExtractor) {
        return new KeyValueExtractor<>(Function.identity(), valueExtractor);
    }
}
