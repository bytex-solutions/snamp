package com.bytex.snamp.io;

import java.io.Serializable;
import java.util.function.BinaryOperator;

/**
 * Represents serializable version of {@link BinaryOperator}.
 * @since 2.0
 * @version 2.1
 */
@FunctionalInterface
public interface SerializableBinaryOperator<T> extends BinaryOperator<T>, Serializable {
}
