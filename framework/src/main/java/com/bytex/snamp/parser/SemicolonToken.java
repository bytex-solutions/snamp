package com.bytex.snamp.parser;

/**
 * Represents semicolon as token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SemicolonToken extends PunctuationToken {
    private static final long serialVersionUID = 6468759490004875338L;
    public static final char VALUE = ';';
    public static final SemicolonToken INSTANCE = new SemicolonToken();

    private SemicolonToken() {
        super(VALUE);
    }
}
