package com.bytex.snamp.connector.composite.functions;

import java.util.function.LongSupplier;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class IntegerToken extends Token implements LongSupplier {
    private IntegerToken(final CharSequence value){
        super(value);
    }

    @Override
    public long getAsLong() {
        return Long.parseLong(toString());
    }


    static boolean isValidCharacter(final char ch){
        return Character.isDigit(ch);
    }

    static Token parse(final CharSequence input, final TokenPosition position) {
        final StringBuilder value = new StringBuilder();
        do {
            char currentChar;
            if (isValidCharacter(currentChar = input.charAt(position.get())))
                value.append(currentChar);
            else
                break;
        } while (position.inc() < input.length());
        return new IntegerToken(value);
    }
}
