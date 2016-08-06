package com.bytex.snamp.jmx;

import java.text.ParseException;

/**
 * Exception indicating that string could not be parsed into JMX simple type.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public final class SimpleTypeParseException extends ParseException {
    private static final long serialVersionUID = -6432237973572577001L;

    SimpleTypeParseException(final String value,
                             final Exception e) {
        super(value, 0);
        initCause(e);
    }
}
