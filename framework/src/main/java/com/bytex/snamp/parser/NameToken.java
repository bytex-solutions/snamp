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

    public NameToken(final String name) {
        this((CharSequence) name);
    }

    private static boolean isValidCharacter(final char ch, final boolean notFirstChar){
        return Character.isLetter(ch) || ch == '_' || (notFirstChar && Character.isDigit(ch));
    }

    static boolean isValidCharacter(final char ch) {
        return isValidCharacter(ch, false);
    }

    private static CharSequence parse(final CharReader reader) throws IOException {
        final StringBuilder name = new StringBuilder();
        boolean notFirstChar = false;
        do {
            final char currentChar = reader.get();
            if (isValidCharacter(currentChar, notFirstChar)) {
                name.append(currentChar);
                reader.skip();
                notFirstChar = true;
            } else
                break;
        } while (reader.getRemaining() > 0);
        return name;
    }
}
