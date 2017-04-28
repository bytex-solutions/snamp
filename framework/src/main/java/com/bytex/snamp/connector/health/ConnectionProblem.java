package com.bytex.snamp.connector.health;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Indicates that network connection between SNAMP and managed resource is not available.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ConnectionProblem extends ResourceMalfunctionStatus {
    private static final long serialVersionUID = -3765564303828054111L;
    private final IOException error;

    public ConnectionProblem(final IOException error, final Instant timeStamp) {
        super(timeStamp);
        this.error = Objects.requireNonNull(error);
    }

    public ConnectionProblem(final IOException error){
        this(error, Instant.now());
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

    @Override
    @Nonnull
    public Level getLevel() {
        return Level.SEVERE;
    }

    private boolean like(final ConnectionProblem status){
        return super.like(status) && status.error.equals(error);
    }

    @Override
    public boolean like(final HealthStatus status) {
        return status instanceof ConnectionProblem && like((ConnectionProblem) status);
    }
}
