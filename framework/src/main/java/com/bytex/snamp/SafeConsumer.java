package com.bytex.snamp;

/**
 * Represents a special version of {@link com.bytex.snamp.Consumer} that doesn't throw
 * any exception.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface SafeConsumer<T> extends Consumer<T, ExceptionPlaceholder> {
    /**
     * Performs this operation on the given argument.
     *
     * @param value The value to process.
     */
    @Override
    void accept(final T value);
}
