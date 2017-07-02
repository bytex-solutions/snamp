package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.core.ScriptletCompilationException;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class InvalidScalingPolicyException extends ScriptletCompilationException {
    private static final long serialVersionUID = -4710155652790586623L;
    private final String language;

    InvalidScalingPolicyException(final String language, final IOException e) {
        super("Unable to download scaling policy", e);
        this.language = language;
    }

    InvalidScalingPolicyException(final String language){
        super(language);
        this.language = language;
    }

    InvalidScalingPolicyException(final String language, final String scriptBody, final Exception e) {
        super("Scaling policy has invalid syntax: " + scriptBody, e);
        this.language = language;
    }

    @Override
    public String getLanguage() {
        return language;
    }
}
