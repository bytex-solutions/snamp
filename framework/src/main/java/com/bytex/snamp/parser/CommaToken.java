package com.bytex.snamp.parser;

/**
 * Represents comma as token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class CommaToken extends PunctuationToken {
    private static final long serialVersionUID = 6468759490004875338L;
    public static final char VALUE = ',';
    public static final CommaToken INSTANCE = new CommaToken();

    private CommaToken() {
        super(VALUE);
    }
}
