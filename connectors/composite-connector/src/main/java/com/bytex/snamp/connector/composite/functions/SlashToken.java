package com.bytex.snamp.connector.composite.functions;

/**
 * Represents slash.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SlashToken extends PunctuationToken {
    static final char VALUE = '/';
    static final SlashToken INSTANCE = new SlashToken();

    private SlashToken() {
        super(VALUE);
    }
}
