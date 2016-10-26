package com.bytex.snamp.parser;

/**
 * Represents $ as token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class DollarToken extends PunctuationToken {
    private static final long serialVersionUID = 1016754921413287342L;
    public static final char VALUE = '$';
    public static final DollarToken INSTANCE = new DollarToken();

    private DollarToken() {
        super('$');
    }
}
