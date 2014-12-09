package com.itworks.snamp.mapping;

/**
 * Represents record reader.
 * @param <I> Type of the record index.
 * @param <R> Type of the record content.
 * @param <E> Type of the exception that can be produced by reader.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface RecordReader<I, R, E extends Exception> {
    /**
     * Processes the single record.
     * @param index An index of the record.
     * @param value A record.
     * @throws E Unable to process record.
     */
    void read(final I index, final R value) throws E;
}
