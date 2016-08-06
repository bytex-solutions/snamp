package com.bytex.snamp.adapters.xmpp;

/**
 * An exception raised by command handler.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
class CommandException extends Exception {
    private static final long serialVersionUID = -2603422421708027372L;

    protected CommandException(final String message) {
        super(message);
    }

    protected CommandException(final Throwable cause) {
        super(cause);
    }
}
