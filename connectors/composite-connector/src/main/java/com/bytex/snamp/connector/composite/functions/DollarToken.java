package com.bytex.snamp.connector.composite.functions;

/**
 * Represents $ as token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DollarToken extends PunctuationToken {
    static final char VALUE = '$';
    static final DollarToken INSTANCE = new DollarToken();

    private DollarToken() {
        super('$');
    }
}
