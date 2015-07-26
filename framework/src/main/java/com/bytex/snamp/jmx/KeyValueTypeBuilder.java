package com.bytex.snamp.jmx;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularType;
import java.util.Objects;

/**
 * Represents a builder of {@link TabularType} that is used to construct key/value pairs.
 * This class cannot be inherited.
 * @param <K> Type of the keys.
 * @param <V> Type of the values.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class KeyValueTypeBuilder<K, V> implements OpenTypeBuilder<TabularType> {
    /**
     * Default name of the column with key.
     */
    public static final String DEFAULT_KEY_COLUMN = "key";
    /**
     * Default name of the column with value.
     */
    public static final String DEFAULT_VALUE_COLUMN = "value";

    /**
     * Default name of the table type.
     */
    public static final String DEFAULT_TYPE_NAME = "KeyValuePair";

    private String typeName = DEFAULT_TYPE_NAME;
    private String typeDescription = "A set of key/value pairs";
    private String keyColumn;
    private String valueColumn;
    private String keyColumnDescr = "Key";
    private String valueColumnDescr = "Value";
    private OpenType<K> keyColumnType;
    private OpenType<V> valueColumnType;

    public KeyValueTypeBuilder(final String keyColumn, final String valueColumn){
        this.keyColumn = Objects.requireNonNull(keyColumn);
        this.valueColumn = Objects.requireNonNull(valueColumn);
    }

    public KeyValueTypeBuilder() {
        this(DEFAULT_KEY_COLUMN, DEFAULT_VALUE_COLUMN);
    }

    public KeyValueTypeBuilder<K, V> setTypeName(final String value){
        typeName = Objects.requireNonNull(value);
        return this;
    }

    public KeyValueTypeBuilder<K, V> setTypeDescription(final String value){
        typeDescription = value;
        return this;
    }

    public KeyValueTypeBuilder<K, V> setKeyColumnName(final String value){
        keyColumn = Objects.requireNonNull(value);
        return this;
    }

    public KeyValueTypeBuilder<K, V> setKeyColumnDescription(final String value){
        keyColumnDescr = Objects.requireNonNull(value);
        return this;
    }

    public KeyValueTypeBuilder<K, V> setKeyColumnType(final OpenType<K> value){
        keyColumnType = Objects.requireNonNull(value);
        return this;
    }

    public KeyValueTypeBuilder<K, V> setKeyColumn(final String name, final String description, final OpenType<K> type) {
        return setKeyColumnName(name).
                setKeyColumnDescription(description)
                .setKeyColumnType(type);
    }

    public KeyValueTypeBuilder<K, V> setValueColumnName(final String value){
        valueColumn = value;
        return this;
    }

    public KeyValueTypeBuilder<K, V> setValueColumnDescription(final String value){
        valueColumnDescr = value;
        return this;
    }

    public KeyValueTypeBuilder<K, V> setValueColumnType(final OpenType<V> value){
        valueColumnType = Objects.requireNonNull(value);
        return this;
    }

    public KeyValueTypeBuilder<K, V> setValueColumn(final String name, final String description, final OpenType<V> type) {
        return setValueColumnName(name).
                setValueColumnDescription(description).
                setValueColumnType(type);
    }

    /**
     * Constructs a new type that holds key/value pairs.
     * @return A new tabular type.
     * @throws OpenDataException Unable to construct type.
     */
    @Override
    public TabularType build() throws OpenDataException {
        return new TabularTypeBuilder()
                .setTypeName(typeName, true)
                .setDescription(typeDescription, true)
                .addColumn(keyColumn, keyColumnDescr, keyColumnType, true)
                .addColumn(valueColumn, valueColumnDescr, valueColumnType, false)
                .build();
    }

    /**
     * Constructs a new type that holds key/value pairs.
     * @return A new tabular type.
     * @throws IllegalStateException Unable to construct type.
     */
    @Override
    public TabularType get() throws IllegalStateException {
        try {
            return build();
        } catch (final OpenDataException e) {
            throw new IllegalStateException(e);
        }
    }
}
