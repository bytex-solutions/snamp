package com.bytex.snamp.parser;

/**
 * Represents left bracket as a token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class RightBracketToken extends PunctuationToken {
    private static final long serialVersionUID = -1210661219600635416L;
    public static final char VALUE = ')';
    public static final RightBracketToken INSTANCE = new RightBracketToken();


    private RightBracketToken() {
        super(VALUE);
    }
}
