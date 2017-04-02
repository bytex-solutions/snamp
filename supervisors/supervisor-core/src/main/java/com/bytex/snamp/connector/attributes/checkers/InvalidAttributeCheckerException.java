package com.bytex.snamp.connector.attributes.checkers;

import java.io.IOException;

/**
 * Occurs if attribute checker cannot be instantiated.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class InvalidAttributeCheckerException extends Exception {
    private static final long serialVersionUID = -2754906759778952794L;

    InvalidAttributeCheckerException(final IOException e) {
        super("Unable to download checker script", e);
    }

    InvalidAttributeCheckerException(final String language) {
        super("Unsupported language " + language);
    }

    InvalidAttributeCheckerException(final String scriptBody, final Exception e) {
        super("Checker script has invalid syntax: " + scriptBody, e);
    }
}
