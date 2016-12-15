package com.bytex.snamp.parser;

import javax.annotation.concurrent.Immutable;

/**
 * Represents . as token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Immutable
public final class DotToken extends PunctuationToken {
    private static final long serialVersionUID = 1016754921413287342L;
    public static final char VALUE = '.';
    public static final DotToken INSTANCE = new DotToken();

    private DotToken() {
        super(VALUE);
    }
}
