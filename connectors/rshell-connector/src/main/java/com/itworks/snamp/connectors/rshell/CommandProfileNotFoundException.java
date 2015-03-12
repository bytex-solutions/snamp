package com.itworks.snamp.connectors.rshell;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CommandProfileNotFoundException extends Exception {
    CommandProfileNotFoundException(final Object profilePath) {
        super(String.format("Command-line profile %s not found", profilePath));
    }
}
