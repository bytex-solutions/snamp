package com.bytex.snamp.connector.composite.functions;

/**
 * Represents identifier token, such as name.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class NameToken extends Token {
    private NameToken(final CharSequence value) {
        super(value);
    }

    static boolean isValidCharacter(final char ch) {
        return Character.isLetter(ch) || ch == '_';
    }

    static NameToken parse(final CharSequence input, final TokenPosition position) {
        final StringBuilder name = new StringBuilder();
        do {
            char currentChar;
            if (isValidCharacter(currentChar = input.charAt(position.get())))
                name.append(currentChar);
            else
                break;
        } while (position.inc() < input.length());
        return new NameToken(name);
    }
}
