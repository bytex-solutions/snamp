package com.bytex.snamp.gateway.xmpp;

/**
 * An exception raised by command handler.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
class CommandException extends Exception {
    private static final long serialVersionUID = -2603422421708027372L;

    protected CommandException(final String message) {
        super(message);
    }

    protected CommandException(final String message, final Object... args){
        this(String.format(message, args));
    }

    protected CommandException(final Throwable cause) {
        super(cause);
    }
}
