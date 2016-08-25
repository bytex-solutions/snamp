package com.bytex.snamp.connector.composite.functions;

/**
 * Represents left bracket as a token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class RightBracketToken extends PunctuationToken {
    static final char VALUE = ')';
    static final RightBracketToken INSTANCE = new RightBracketToken();

    private RightBracketToken() {
        super(VALUE);
    }
}
