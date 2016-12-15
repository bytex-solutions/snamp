package com.bytex.snamp.parser;

import javax.annotation.concurrent.Immutable;

/**
 * Represents colon as token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Immutable
public final class ColonToken extends PunctuationToken {
    private static final long serialVersionUID = 6468759490004875338L;
    public static final char VALUE = ':';
    public static final ColonToken INSTANCE = new ColonToken();

    private ColonToken() {
        super(VALUE);
    }
}
