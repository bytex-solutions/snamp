package com.itworks.snamp.connectors;

import com.itworks.snamp.mapping.RecordReader;

import java.util.Objects;

/**
 * Represents map reader.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class MapReader<E extends Exception> implements RecordReader<String, Object, E> {
    private final ManagedEntityTabularType tabularType;

    /**
     * Initializes a new map reader.
     * @param mapTypeDescriptor Map type descriptor. Cannot be {@literal null}.
     */
    protected MapReader(final ManagedEntityTabularType mapTypeDescriptor){
        tabularType = Objects.requireNonNull(mapTypeDescriptor);
    }

    /**
     * Reads the map entry.
     *
     * @param key A key that identifies the map value.
     * @param value A map value.
     * @throws E Unable to process map entry.
     */
    @Override
    public final void read(final String key, final Object value) throws E {
        read(key, new ManagedEntityValue<>(value, tabularType.getColumnType(key)));
    }

    /**
     * Reads the map entry.
     * @param key A key that identifies the map value.
     * @param value The value of the map with its type.
     * @throws E Unable to process map entry.
     */
    protected abstract void read(final String key,
                                 final ManagedEntityValue<?> value) throws E;
}
