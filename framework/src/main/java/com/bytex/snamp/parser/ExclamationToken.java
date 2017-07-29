package com.bytex.snamp.parser;

/**
 * Represents <it>!</it> as punctuation token.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ExclamationToken extends PunctuationToken implements SingleCharacterToken {
    public static final char VALUE = '!';
    public static final int TYPE = VALUE;
    public static final ExclamationToken INSTANCE = new ExclamationToken();
    private static final long serialVersionUID = -834707990777932840L;

    private ExclamationToken(){
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
