package com.bytex.snamp.parser;

import javax.annotation.concurrent.Immutable;

/**
 * Represents slash.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Immutable
public final class SlashToken extends PunctuationToken implements SingleCharacterToken {
    private static final long serialVersionUID = 1143234177279410559L;
    public static final char VALUE = '/';
    public static final SlashToken INSTANCE = new SlashToken();

    private SlashToken() {
        super(VALUE);
    }

    /**
     * Gets character wrapped by this token.
     *
     * @return Wrapped character.
     */
    @Override
    public char getValue() {
        return VALUE;
    }
}
