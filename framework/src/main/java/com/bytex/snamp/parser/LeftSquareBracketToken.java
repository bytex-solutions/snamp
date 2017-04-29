package com.bytex.snamp.parser;

/**
 * Represents left square bracket as a token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class LeftSquareBracketToken extends PunctuationToken implements SingleCharacterToken {
    public static final char VALUE = '[';
    public static final int TYPE = VALUE;
    public static final LeftSquareBracketToken INSTANCE = new LeftSquareBracketToken();
    private static final long serialVersionUID = 4339841606856689663L;

    private LeftSquareBracketToken() {
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
