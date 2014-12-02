package com.itworks.snamp.mapping;

import com.google.common.collect.ObjectArrays;
import com.itworks.snamp.ExceptionPlaceholder;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Represents a set of helper methods related to {@link com.itworks.snamp.mapping.RecordSet} interface.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RecordSetUtils {
    private RecordSetUtils() {
    }

    /**
     * Creates a new instance of the empty record set.
     *
     * @param <I> Type of the record index.
     * @param <R> Type of the record content.
     * @return A new empty record set.
     */
    public static <I, R> RecordSet<I, R> emptySet() {
        return new RecordSet<I, R>() {
            @Override
            public <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) {

            }

            @Override
            public int size() {
                return 0;
            }

            /**
             * Returns an equivalent object that is parallel.
             * May return itself, either because the object was already parallel,
             * or because the underlying object state was modified to be parallel.
             *
             * @param executor An executor used to execute methods in parallel manner.
             * @return An object that supports parallel execution of some methods.
             */
            @Override
            public RecordSet<I, R> parallel(final ExecutorService executor) {
                return this;
            }

            /**
             * Returns an equivalent object that is sequential.
             * May return itself, either because the object was already sequential,
             * or because the underlying object state was modified to be sequential.
             *
             * @return An object that supports sequential execution of some methods.
             */
            @Override
            public RecordSet<I, R> sequential() {
                return this;
            }
        };
    }

    /**
     * Converts an array into the record set.
     *
     * @param array An array to convert.
     * @param <V>   Type of the array elements.
     * @return A record set that wraps the underlying array.
     */
    public static <V> RecordSet<Integer, V> fromArray(final V[] array) {
        return array == null || array.length == 0 ?
                RecordSetUtils.<Integer, V>emptySet() :
                new OrdinalRecordSet<Integer, V>() {
                    @Override
                    protected Integer first() {
                        return 0;
                    }

                    @Override
                    protected Integer next(final Integer index) {
                        return index < size() - 1 ? index + 1 : null;
                    }

                    @Override
                    protected V getRecord(final Integer index) {
                        return array[index];
                    }

                    @Override
                    public int size() {
                        return array.length;
                    }
                };
    }

    /**
     * Converts a list into the record set.
     *
     * @param list A list to convert.
     * @param <R>  Type of the records in the set.
     * @return A record set that wraps the underlying list.
     */
    public static <R> RecordSet<Integer, R> fromList(final List<R> list) {
        return list == null || list.isEmpty() ?
                RecordSetUtils.<Integer, R>emptySet() :
                new OrdinalRecordSet<Integer, R>() {
                    @Override
                    protected Integer first() {
                        return 0;
                    }

                    @Override
                    protected Integer next(final Integer index) {
                        return index < size() - 1 ? index + 1 : null;
                    }

                    @Override
                    protected R getRecord(final Integer index) {
                        return list.get(index);
                    }

                    @Override
                    public int size() {
                        return list.size();
                    }
                };
    }

    /**
     * Converts a map into the record set.
     *
     * @param map A map to convert.
     * @param <K> Type of the keys in the map.
     * @param <V> Type of the values in the map.
     * @return A new record set that wraps the underlying map.
     */
    public static <K, V> RecordSet<K, V> fromMap(final Map<K, V> map) {
        return map == null || map.isEmpty() ?
                RecordSetUtils.<K, V>emptySet() :
                new KeyedRecordSet<K, V>() {
                    @Override
                    protected Set<K> getKeys() {
                        return map.keySet();
                    }

                    @Override
                    protected V getRecord(final K key) {
                        return map.get(key);
                    }
                };
    }

    /**
     * Fills the map using the specified record set.
     * @param input The record set used as a source for map entries. Cannot be {@literal null}.
     * @param output A map to be populated using the record set. Cannot be {@literal null}.
     * @param <K> Type of the map keys.
     * @param <V> Type of the map values.
     * @throws Exception Unable to read data from the record set.
     */
    public static <K, V> void fillMap(final RecordSet<K, V> input, final Map<K, V> output) throws Exception {
        input.sequential().forEach(new RecordReader<K, V, ExceptionPlaceholder>() {
            @Override
            public void read(final K index, final V value) {
                output.put(index, value);
            }
        });
    }

    /**
     * Converts a record set into the map.
     * @param input A record set to be converted into the map.
     * @param <K> Type of the map keys.
     * @param <V> Type of the map values.
     * @throws Exception Unable to read data from the record set.
     */
    public static <K, V> Map<K, V> toMap(final RecordSet<K, V> input) throws Exception{
        final Map<K, V> result = new HashMap<>(input.size());
        fillMap(input, result);
        return result;
    }

    private static <V> V[] toArrayCore(final RecordSet<? extends Number, V> input, final Class<V> elementType) throws Exception{
        final V[] result = ObjectArrays.newArray(elementType, input.size());
        input.forEach(new RecordReader<Number, V, ExceptionPlaceholder>() {
            @Override
            public void read(final Number index, final V value) {
                result[index.intValue()] = value;
            }
        });
        return result;
    }

    public static <V> V[] toArray(final RecordSet<? extends Number, V> input,
                                  final Class<V> elementType) throws Exception{
        return toArrayCore(input.sequential(), elementType);
    }

    public static <V> V[] toArray(final RecordSet<? extends Number, V> input,
                                  final Class<V> elementType,
                                  final ExecutorService executor) throws Exception{
        return executor == null ? toArray(input, elementType) : toArrayCore(input.parallel(executor), elementType);
    }

    /**
     * Extracts type of the exception declared in the record reader.
     * @param reader The record reader. Cannot be {@literal null}.
     * @param <E> The exception that may be thrown by reader.
     * @return A {@link java.lang.Class} that represents an exception; or {@literal null} if exception cannot be determined.
     */
    public static <E extends Exception> Class<E> getExceptionType(final RecordReader<?, ?, E> reader){
        return RecordReaderUtils.getExceptionType(reader.getClass());
    }
}
