package com.itworks.snamp.connectors.rshell;

/**
 *
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class UndefinedCommandProfileException extends Exception {
    UndefinedCommandProfileException(){
        super("Command profile is not specified");
    }
}
