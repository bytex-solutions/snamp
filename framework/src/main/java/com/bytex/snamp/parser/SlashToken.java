package com.bytex.snamp.parser;

/**
 * Represents slash.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SlashToken extends PunctuationToken {
    private static final long serialVersionUID = 1143234177279410559L;
    public static final char VALUE = '/';
    public static final SlashToken INSTANCE = new SlashToken();

    private SlashToken() {
        super(VALUE);
    }
}
