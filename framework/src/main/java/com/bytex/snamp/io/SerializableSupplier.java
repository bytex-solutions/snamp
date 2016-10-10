package com.bytex.snamp.io;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Represents serializable version of {@link Supplier}.
 * @since 2.0
 * @version 2.0
 */
@FunctionalInterface
public interface SerializableSupplier<T> extends Supplier<T>, Serializable {
}
