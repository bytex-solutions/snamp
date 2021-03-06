package com.bytex.snamp.gateway.xmpp;

/**
 * Invalid command arguments.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class InvalidCommandFormatException extends CommandException {
    private static final long serialVersionUID = -2655485274809089455L;

    InvalidCommandFormatException(){
        super("Invalid command format");
    }
}
