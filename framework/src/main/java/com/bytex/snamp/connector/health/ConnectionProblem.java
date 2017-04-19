package com.bytex.snamp.connector.health;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents some connection problems.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ConnectionProblem extends ResourceMalfunction {
    public static final int CODE = 1;
    private static final long serialVersionUID = -3765564303828054111L;
    private final IOException error;

    public ConnectionProblem(final String resourceName, final IOException error) {
        super(resourceName, CODE, true);
        this.error = Objects.requireNonNull(error);
    }

    public IOException getError(){
        return error;
    }

    /**
     * Returns the localized description of this object.
     *
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this object.
     */
    @Override
    public String toString(final Locale locale) {
        return String.format("Connection problems detected. Caused by %s", error);
    }
}
