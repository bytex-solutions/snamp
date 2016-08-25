package com.bytex.snamp.connector.composite.functions;

/**
 * Represents token for punctuation.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class PunctuationToken extends Token {
    PunctuationToken(final char value) {
        super(String.valueOf(value));
    }
}
