package com.bytex.snamp.connector.composite.functions;

/**
 * Represents comma as token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CommaToken extends PunctuationToken {
    static final char VALUE = ',';
    static final CommaToken INSTANCE = new CommaToken();

    private CommaToken() {
        super(VALUE);
    }
}
