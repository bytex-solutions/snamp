package com.bytex.snamp;

import java.util.function.BiConsumer;

/**
 * Represents record reader.
 * @param <I> Type of the record index.
 * @param <R> Type of the record content.
 * @param <E> Type of the exception that can be produced by reader.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@FunctionalInterface
public interface EntryReader<I, R, E extends Throwable> {
    /**
     * Processes the single record.
     * @param index An index of the record.
     * @param value A record.
     * @throws E Unable to process record.
     * @return {@literal true} to continue iteration; {@literal false} to abort iteration
     */
    boolean accept(final I index, final R value) throws E;

    static <I, R> EntryReader<? super I, ? super R, ExceptionPlaceholder> fromConsumer(final BiConsumer<? super I, ? super R> consumer) {
        return (index, record) -> {
            consumer.accept(index, record);
            return true;
        };
    }
}
