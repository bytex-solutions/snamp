package com.bytex.snamp.connectors.rshell;

/**
 *
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class UndefinedCommandProfileException extends Exception {
    UndefinedCommandProfileException(){
        super("Command profile is not specified");
    }
}
