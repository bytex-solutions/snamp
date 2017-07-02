package com.bytex.snamp.parser;

/**
 * Represents <it>=</it> as punctuation token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class EQToken extends PunctuationToken implements SingleCharacterToken {
    public static final char VALUE = '=';
    public static final int TYPE = VALUE;
    public static final EQToken INSTANCE = new EQToken();
    private static final long serialVersionUID = -8494050017099826260L;

    private EQToken(){
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
