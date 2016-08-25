package com.bytex.snamp.connector.composite.functions;

import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Represents parser token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class Token implements CharSequence {
    private final CharSequence value;

    Token(final CharSequence value){
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public final int length() {
        return value.length();
    }

    @Override
    public final char charAt(final int index) {
        return value.charAt(index);
    }

    @Override
    public final CharSequence subSequence(final int start, final int end) {
        return value.subSequence(start, end);
    }

    @Override
    public final IntStream chars() {
        return value.chars();
    }

    @Override
    public final IntStream codePoints() {
        return value.codePoints();
    }

    @Override
    public final int hashCode() {
        return value.hashCode();
    }

    private boolean equals(final Token other){
        return getClass().isInstance(other) && value.equals(other.value);
    }

    @Override
    public final boolean equals(final Object other) {
        return other instanceof Token && equals((Token) other);
    }

    @Override
    public final String toString() {
        return value.toString();
    }
}
