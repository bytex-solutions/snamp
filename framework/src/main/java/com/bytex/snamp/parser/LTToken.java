package com.bytex.snamp.parser;

/**
 * Represents &lt; as punctuation token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class LTToken extends PunctuationToken implements SingleCharacterToken {
    public static final char VALUE = '<';
    public static final int TYPE = VALUE;
    public static final LTToken INSTANCE = new LTToken();
    private static final long serialVersionUID = -2013614842119652153L;

    private LTToken(){
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
