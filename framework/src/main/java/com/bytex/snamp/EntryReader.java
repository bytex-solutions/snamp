package com.bytex.snamp;

/**
 * Represents record reader.
 * @param <I> Type of the record index.
 * @param <R> Type of the record content.
 * @param <E> Type of the exception that can be produced by reader.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@FunctionalInterface
public interface EntryReader<I, R, E extends Exception> {
    /**
     * Processes the single record.
     * @param index An index of the record.
     * @param value A record.
     * @throws E Unable to process record.
     * @return {@literal true} to continue iteration; {@literal false} to abort iteration
     */
    boolean read(final I index, final R value) throws E;
}
