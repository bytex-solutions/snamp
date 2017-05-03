package com.bytex.snamp.supervision.health.triggers;

import com.bytex.snamp.core.ScriptletCompilationException;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class InvalidTriggerException extends ScriptletCompilationException {
    private static final long serialVersionUID = 6236586157113008514L;
    private final String language;

    InvalidTriggerException(final String language, final IOException e) {
        super("Unable to download trigger script", e);
        this.language = language;
    }

    InvalidTriggerException(final String language){
        super(language);
        this.language = language;
    }

    InvalidTriggerException(final String language, final String scriptBody, final Exception e) {
        super("Trigger script has invalid syntax: " + scriptBody, e);
        this.language = language;
    }

    @Override
    public String getLanguage() {
        return language;
    }
}
