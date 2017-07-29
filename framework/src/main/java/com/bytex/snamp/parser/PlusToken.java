package com.bytex.snamp.parser;

/**
 * Represents <it>+</it> as punctuation token.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class PlusToken extends PunctuationToken implements SingleCharacterToken {
    public static final char VALUE = '+';
    public static final int TYPE = VALUE;
    public static final PlusToken INSTANCE = new PlusToken();
    private static final long serialVersionUID = -160107926339929295L;

    private PlusToken(){
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
