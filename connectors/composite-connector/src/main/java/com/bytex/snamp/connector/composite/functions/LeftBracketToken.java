package com.bytex.snamp.connector.composite.functions;

/**
 * Represents left bracket as a token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class LeftBracketToken extends PunctuationToken {
    static final char VALUE = '(';
    static final LeftBracketToken INSTANCE = new LeftBracketToken();

    private LeftBracketToken() {
        super(VALUE);
    }
}
