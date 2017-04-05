package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.core.ScriptletCompilationException;

import java.io.IOException;

/**
 * Occurs if attribute checker cannot be instantiated.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class InvalidAttributeCheckerException extends ScriptletCompilationException {
    private static final long serialVersionUID = -2754906759778952794L;
    private final String language;

    InvalidAttributeCheckerException(final String language, final IOException e) {
        super("Unable to download checker script", e);
        this.language = language;
    }

    InvalidAttributeCheckerException(final String language) {
        super(language);
        this.language = language;
    }

    InvalidAttributeCheckerException(final String language, final String scriptBody, final Exception e) {
        super("Checker script has invalid syntax: " + scriptBody, e);
        this.language = language;
    }

    @Override
    public String getLanguage() {
        return language;
    }
}
