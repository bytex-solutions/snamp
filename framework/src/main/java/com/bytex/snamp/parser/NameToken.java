package com.bytex.snamp.parser;

import java.io.IOException;

/**
 * Represents identifier token, such as name.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class NameToken extends Token {
    private static final long serialVersionUID = 4820538603021200807L;

    NameToken(final CharReader reader) throws IOException {
        this(parse(reader));
    }

    protected NameToken(final CharSequence name){
        super(name);
    }

    static boolean isValidCharacter(final char ch) {
        return Character.isLetter(ch) || ch == '_';
    }

    private static CharSequence parse(final CharReader reader) throws IOException {
        final StringBuilder name = new StringBuilder();
        do {
            final char currentChar = reader.get();
            if (isValidCharacter(currentChar)) {
                name.append(currentChar);
                reader.skip();
            } else
                break;
        } while (reader.getRemaining() > 0);
        return name;
    }
}
