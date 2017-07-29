package com.bytex.snamp.parser;

/**
 * Unexpected character detected.
 * @since 2.0
 * @version 2.1
 */
public final class UnexpectedCharException extends ParseException {
    private static final long serialVersionUID = 2263767034329625213L;
    private final char ch;

    public UnexpectedCharException(final char unexpectedChar){
        super(String.format("Unexpected character detected: %s", unexpectedChar));
        ch = unexpectedChar;
    }

    public char getChar(){
        return ch;
    }
}
