package com.bytex.snamp.parser;

/**
 * Represents left bracket as a token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class LeftBracketToken extends PunctuationToken {
    public static final char VALUE = '(';
    public static final LeftBracketToken INSTANCE = new LeftBracketToken();
    private static final long serialVersionUID = 4339841606856689663L;

    private LeftBracketToken() {
        super(VALUE);
    }
}
