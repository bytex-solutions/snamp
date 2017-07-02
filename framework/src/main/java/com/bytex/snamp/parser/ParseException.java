package com.bytex.snamp.parser;

import java.io.IOException;

/**
 * Represents exception during parsing stream.
 * @since 2.0
 * @version 2.0
 */
public class ParseException extends Exception {
    private static final long serialVersionUID = 2520656357861944470L;

    protected ParseException(final String message) {
        super(message);
    }

    ParseException(final IOException e){
        super("I/O error", e);
    }

    ParseException(){
        super("End of token stream");
    }
}
