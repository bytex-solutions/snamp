package com.bytex.snamp.connector.supervision.triggers;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class InvalidTriggerException extends Exception {
    private static final long serialVersionUID = 6236586157113008514L;

    InvalidTriggerException(final IOException e) {
        super("Unable to download trigger script", e);
    }

    InvalidTriggerException(final String scriptBody, final Exception e) {
        super("Trigger script has invalid syntax: " + scriptBody, e);
    }
}
