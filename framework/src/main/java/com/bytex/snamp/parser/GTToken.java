package com.bytex.snamp.parser;

/**
 * Represents &gt; as punctuation token.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class GTToken extends PunctuationToken implements SingleCharacterToken {
    public static final char VALUE = '>';
    public static final int TYPE = VALUE;
    public static final GTToken INSTANCE = new GTToken();
    private static final long serialVersionUID = -2025336613779082837L;

    private GTToken(){
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
