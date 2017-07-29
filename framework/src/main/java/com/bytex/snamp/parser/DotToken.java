package com.bytex.snamp.parser;

import javax.annotation.concurrent.Immutable;

/**
 * Represents . as token.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Immutable
public final class DotToken extends PunctuationToken implements SingleCharacterToken {
    private static final long serialVersionUID = 1016754921413287342L;
    public static final char VALUE = '.';
    public static final int TYPE = VALUE;
    public static final DotToken INSTANCE = new DotToken();

    private DotToken() {
        super(TYPE, VALUE);
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
