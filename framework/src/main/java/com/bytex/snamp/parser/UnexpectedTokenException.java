package com.bytex.snamp.parser;

import java.util.Objects;

/**
 * Represents unexpected token.
 * @since 2.0
 * @version 2.0
 */
public final class UnexpectedTokenException extends ParseException {
    private static final long serialVersionUID = 9134610173169882334L;
    private final Token token;

    public UnexpectedTokenException(final Token unexpectedToken){
        super(String.format("Unexpected token detected: %s", unexpectedToken));
        token = Objects.requireNonNull(unexpectedToken);
    }

    public Token getToken(){
        return token;
    }
}
