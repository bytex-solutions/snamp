package com.bytex.snamp.parser;

import javax.annotation.concurrent.Immutable;

/**
 * Represents left bracket as a token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Immutable
public final class RightBracketToken extends PunctuationToken implements SingleCharacterToken {
    private static final long serialVersionUID = -1210661219600635416L;
    public static final char VALUE = ')';
    public static final int TYPE = VALUE;
    public static final RightBracketToken INSTANCE = new RightBracketToken();


    private RightBracketToken() {
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
