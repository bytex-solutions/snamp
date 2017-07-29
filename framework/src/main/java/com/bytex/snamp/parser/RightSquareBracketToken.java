package com.bytex.snamp.parser;

/**
 * Represents right square bracket as a token.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class RightSquareBracketToken extends PunctuationToken implements SingleCharacterToken {
    public static final char VALUE = ']';
    public static final int TYPE = VALUE;
    public static final RightSquareBracketToken INSTANCE = new RightSquareBracketToken();
    private static final long serialVersionUID = 4339841606856689663L;

    private RightSquareBracketToken() {
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
