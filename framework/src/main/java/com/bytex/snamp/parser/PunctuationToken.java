package com.bytex.snamp.parser;

/**
 * Represents token for punctuation.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class PunctuationToken extends Token {
    private static final long serialVersionUID = 559010308937454029L;

    protected PunctuationToken(final int type, final char... value) {
        super(type, String.valueOf(value));
    }
}
