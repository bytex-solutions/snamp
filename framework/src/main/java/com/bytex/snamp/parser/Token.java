package com.bytex.snamp.parser;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.bytex.snamp.io.IOUtils.contentAreEqual;

/**
 * Represents abstract class for all tokens.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class Token implements CharSequence, Serializable {
    private static final long serialVersionUID = -3845708480333446910L;
    private final CharSequence value;
    private final int type;

    protected Token(final int type, final CharSequence value){
        this.value = Objects.requireNonNull(value);
        this.type = type;
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

    private boolean equals(final Token other) {
        return getClass().isInstance(other) && contentAreEqual(value, other.value);
    }

    public final int getType(){
        return type;
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
