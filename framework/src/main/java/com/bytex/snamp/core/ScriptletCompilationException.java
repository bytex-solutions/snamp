package com.bytex.snamp.core;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class ScriptletCompilationException extends Exception {
    private static final long serialVersionUID = -4338542410590560917L;

    protected ScriptletCompilationException(final String message, final Throwable cause){
        super(message, cause);
    }

    protected ScriptletCompilationException(final String language){
        super("Unsupported language " + language);
    }

    public abstract String getLanguage();
}
