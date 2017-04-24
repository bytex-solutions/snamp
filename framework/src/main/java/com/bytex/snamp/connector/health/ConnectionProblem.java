package com.bytex.snamp.connector.health;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents some connection problems.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ConnectionProblem extends ResourceMalfunctionStatus {
    private static final long serialVersionUID = -3765564303828054111L;
    private final IOException error;

    public ConnectionProblem(final String resourceName, final IOException error, final Instant timeStamp) {
        super(resourceName, SEVERITY + 1, timeStamp);
        this.error = Objects.requireNonNull(error);
    }

    public ConnectionProblem(final String resourceName, final IOException error){
        this(resourceName, error, Instant.now());
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

    /**
     * Indicates that resource is in critical state (potentially unavailable).
     *
     * @return {@literal true}, if managed resource is in critical state; otherwise, {@literal false}.
     */
    @Override
    public boolean isCritical() {
        return true;
    }
}
