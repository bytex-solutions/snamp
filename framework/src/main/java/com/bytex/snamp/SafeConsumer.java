package com.bytex.snamp;

import java.util.function.Function;

/**
 * Represents a special version of {@link com.bytex.snamp.Consumer} that doesn't throw
 * any exception.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface SafeConsumer<T> extends Consumer<T, ExceptionPlaceholder>, java.util.function.Consumer<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param value The value to process.
     */
    @Override
    void accept(final T value);

    @Override
    default <I> SafeConsumer<I> changeConsumingType(final Function<I, T> transformation){
        return inp -> accept(transformation.apply(inp));
    }
}
