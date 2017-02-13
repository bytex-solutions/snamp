package com.bytex.snamp.parser;

/**
 * Represents <it>-</it> as punctuation token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MinusToken extends PunctuationToken implements SingleCharacterToken {
    public static final char VALUE = '-';
    public static final MinusToken INSTANCE = new MinusToken();
    private static final long serialVersionUID = -160107926339929295L;

    private MinusToken(){
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
