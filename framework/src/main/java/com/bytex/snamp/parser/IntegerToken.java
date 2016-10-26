package com.bytex.snamp.parser;

import java.io.IOException;
import java.util.function.LongSupplier;

/**
 * Represents integer as token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class IntegerToken extends Token implements LongSupplier {
    private static final long serialVersionUID = 5296717972150866303L;

    protected IntegerToken(final CharSequence value){
        super(value);
    }

    IntegerToken(final CharReader reader) throws IOException {
        this(parse(reader));
    }

    @Override
    public long getAsLong() {
        return Long.parseLong(toString());
    }

    static boolean isValidCharacter(final char ch){
        return Character.isDigit(ch);
    }

    private static Token parse(final CharReader reader) throws IOException {
        final StringBuilder value = new StringBuilder();
        do {
            final char currentChar = reader.get();
            if (isValidCharacter(currentChar)) {
                value.append(currentChar);
                reader.skip();
            } else
                break;
        } while (reader.getRemaining() > 0);
        return new IntegerToken(value);
    }
}
