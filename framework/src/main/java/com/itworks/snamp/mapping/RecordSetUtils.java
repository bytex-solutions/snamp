package com.itworks.snamp.mapping;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import com.itworks.snamp.Box;
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

    /**
     * Creates a new instance of the empty row set.
     * @param <C> Type of the cell in the row set.
     * @return A new empty row set.
     */
    public static <C> RowSet<C> emptyRowSet(final Set<String> columns) {
        return new RowSet<C>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public Set<String> getColumns() {
                return columns;
            }

            @Override
            public boolean isIndexed(final String columnName) {
                return false;
            }

            @Override
            public RowSet<C> parallel(final ExecutorService executor) {
                return this;
            }

            @Override
            public RowSet<C> sequential() {
                return this;
            }

            @Override
            public <E extends Exception> void forEach(final RecordReader<? super Integer, ? super RecordSet<String, C>, E> reader) throws E {

            }
            @Override
            public String toString() {
                return String.format(String.format("EmptyRowSet, columns=%s", Joiner.on(", ").join(getColumns())));
            }
        };
    }

    public static <C> RowSet<C> emptyRowSet(final String... columns){
        return emptyRowSet(ImmutableSet.copyOf(columns));
    }

    /**
     * Creates a new instance of the empty row set without columns.
     * @param <C> Type of the cells in the row.
     * @return A new empty row set.
     */
    public static <C> RowSet<C> emptyRowSet(){
        return emptyRowSet(Collections.<String>emptySet());
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
           public <E extends Exception> void forEach(final RecordReader<? super I, ? super R, E> reader) throws E {

           }

           @Override
           public int size() {
               return 0;
           }

           @Override
           public RecordSet<I, R> parallel(final ExecutorService executor) {
               return this;
           }

           @Override
           public RecordSet<I, R> sequential() {
               return this;
           }

           @Override
           public String toString() {
               return "EmptyRecordSet";
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

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
                    }
                };
    }

    public static <I, O> RecordSet<Integer, O> fromArray(final I[] array, final Function<I, O> transformation){
        return array == null || array.length == 0 ?
                RecordSetUtils.<Integer, O>emptySet():
                new OrdinalRecordSet<Integer, O>() {
                    @Override
                    protected Integer first() {
                        return 0;
                    }

                    @Override
                    protected Integer next(final Integer index) {
                        return index < size() - 1 ? index + 1 : null;
                    }

                    @Override
                    protected O getRecord(final Integer index) {
                        return transformation.apply(array[index]);
                    }

                    @Override
                    public int size() {
                        return array.length;
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
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

                    @Override
                    public String toString() {
                        return list.toString();
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

                    @Override
                    public String toString() {
                        return map.toString();
                    }
                };
    }

    /**
     * Fills the map using the specified record set.
     * @param input The record set used as a source for map entries. Cannot be {@literal null}.
     * @param output A map to be populated using the record set. Cannot be {@literal null}.
     * @param <K> Type of the map keys.
     * @param <V> Type of the map values.
     */
    public static <K, V> void fillMap(final RecordSet<? extends K, ? extends V> input, final Map<K, V> output) {
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
     */
    public static <K, V> Map<K, V> toMap(final RecordSet<K, V> input){
        final Map<K, V> result = new HashMap<>(input.size());
        fillMap(input, result);
        return result;
    }

    private static <V> V[] toArrayCore(final RecordSet<? extends Number, V> input, final Class<V> elementType){
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
                                  final Class<V> elementType){
        return toArrayCore(input.sequential(), elementType);
    }

    public static <V> V[] toArray(final RecordSet<? extends Number, V> input,
                                  final Class<V> elementType,
                                  final ExecutorService executor){
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

    /**
     * Constructs a new row set using the specified list of rows.
     * @param <C> Type of the cells in the row.
     * @param columns A set of columns.
     * @param rows A list of rows.
     * @return A new instance of the row set.
     */
    public static <C> RowSet<C> fromRows(final Set<String> columns,
                                     final List<? extends Map<String, C>> rows){
        return fromRows(columns, Collections.<String>emptySet(), rows);
    }

    /**
     * Constructs a new row set using the specified list of rows.
     * @param <C> Type of the cells in the row.
     * @param columns A set of columns.
     * @param rows A list of rows.
     * @param indexedColumns A set of indexed columns.
     * @return A new instance of the row set.
     */
    public static <C> RowSet<C> fromRows(final Set<String> columns,
                                             final Set<String> indexedColumns,
                                             final List<? extends Map<String, C>> rows) {
        return new AbstractRowSet<C>() {
            @Override
            protected C getCell(final String columnName, final int rowIndex) {
                final Map<String, C> row = rows.get(rowIndex);
                return row.get(columnName);
            }

            @Override
            public Set<String> getColumns() {
                return columns;
            }

            @Override
            public boolean isIndexed(final String columnName) {
                return indexedColumns.contains(columnName);
            }

            @Override
            public int size() {
                return rows.size();
            }

            @Override
            public String toString() {
                return String.format("RowSet size=%s, columns={%s}", size(), Joiner.on(", ").join(getColumns()));
            }
        };
    }

    /**
     * Creates a new instance of the row set with single row.
     * @param row A row to be added into the row set.
     * @return A new instance of the row set.
     */
    public static <C> RowSet<C> singleRow(final Map<String, C> row) {
        return fromRows(row.keySet(), Collections.singletonList(row));
    }

    /**
     * Creates a new lazy row set which omits the row with the specified index.
     * @param set A row set.
     * @param rowIndex An index of the row to be omitted.
     * @param <C> Type of the cells in the row.
     * @return A new row set with filtered row.
     */
    public static <C> RowSet<C> removeRow(final RowSet<C> set, final int rowIndex) {
        if (set.size() == 1 && rowIndex == 0)
            return emptyRowSet(set.getColumns());
        else if (rowIndex < set.size())
            return new RowSetWithDeletedRow<>(set, rowIndex);
        else return set;
    }

    public static <C> RowSet<C> insertRow(final RowSet<C> set,
                                    final RecordSet<String, C> row,
                                    final int rowIndex){
        if(set.size() == 0 && rowIndex == 0)
            return singleRow(toMap(row));
        else if(rowIndex <= set.size())
            return new RowSetWithInsertedRow<>(set, row, rowIndex);
        else return set;
    }

    public static <C> RowSet<C> insertRow(final RowSet<C> set,
                                          final Map<String, C> row,
                                          final int rowIndex){
        return insertRow(set, fromMap(row), rowIndex);
    }

    public static <C> RowSet<C> addRow(final RowSet<C> set,
                                       final RecordSet<String, C> row){
        return insertRow(set, row, set.size());
    }

    public static <C> RowSet<C> addRow(final RowSet<C> set,
                                       final Map<String, C> row){
        return addRow(set, fromMap(row));
    }

    public static <C> RowSet<C> setRow(final RowSet<C> set,
                                       final RecordSet<String, C> row,
                                       final int rowIndex){
        if(set.size() == 0)
            return emptyRowSet(set.getColumns());
        else if(set.size() == 1 && rowIndex == 0)
            return singleRow(toMap(row));
        else if(rowIndex < set.size())
            return new RowSetWithModifiedRow<>(set, row, rowIndex);
        else return set;
    }

    public static <C> RowSet<C> setRow(final RowSet<C> set,
                                       final Map<String, C> row,
                                       final int rowIndex){
        return setRow(set, fromMap(row), rowIndex);
    }

    /**
     * Converts row set to list of rows.
     * @param set A row set to convert.
     * @param <C> Type of the cells in the row set.
     * @return A list of rows.
     */
    public static <C> List<? extends Map<String, C>> toList(final RowSet<C> set){
        final List<Map<String, C>> result = new ArrayList<>(set.size());
        set.forEach(new RecordReader<Integer, RecordSet<String, C>, ExceptionPlaceholder>() {
            @Override
            public void read(final Integer index, final RecordSet<String, C> value) {
                result.add(toMap(value));
            }
        });
        return result;
    }

    /**
     * Gets the row at the specified index.
     * @param set The row set.
     * @param index An index of the row.
     * @param <C> Type of the cells in the row.
     * @return The row in the set; or {@literal null} if index is invalid.
     */
    public static <C> RecordSet<String, C> getRow(final RowSet<C> set,
                                                  final int index){
        final Box<RecordSet<String, C>> result = new Box<>();
        set.sequential().forEach(new RecordReader<Integer, RecordSet<String, C>, ExceptionPlaceholder>() {
            @Override
            public void read(final Integer rowIndex, final RecordSet<String, C> value) {
                if(rowIndex == index)
                    result.set(value);
            }
        });
        return result.get();
    }

    /**
     * Transforms each record in the record set.
     * @param set The record set to transform.
     * @param transformation Record transformation.
     * @param <I> Type of the record index.
     * @param <V1> Type of the input record.
     * @param <V2> Type of the output record.
     * @return A new record set with transformed record values.
     */
    public static <I, V1, V2> RecordSet<I, V2> transformRecords(final RecordSet<I, V1> set,
                                                         final Function<V1, V2> transformation) {
        return transform(set, Functions.<I>identity(), transformation);
    }

    public static <I1, I2, V> RecordSet<I2, V> transformIndexes(final RecordSet<I1, V> set,
                                                                final Function<I1, I2> transformation){
        return transform(set, transformation, Functions.<V>identity());
    }

    public static <I1, I2, V1, V2> RecordSet<I2, V2> transform(final RecordSet<I1, V1> set,
                                                               final Function<I1, I2> indexTransf,
                                                               final Function<V1, V2> recordTransf){
        if(set == null) return null;
        else if(set.size() == 0) return emptySet();
        else return new TransformedRecordSet<>(set, indexTransf, recordTransf);
    }
}
