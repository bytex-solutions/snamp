package com.bytex.snamp.connector.supervision;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Indicates that everything is OK.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OkStatus extends HealthStatus {
    public static final int CODE = 0;
    public static final OkStatus INSTANCE = new OkStatus();
    private static final long serialVersionUID = 5391122005596632004L;

    private OkStatus() {
        super("", CODE);
    }

    @Override
    public boolean isCritical() {
        return false;
    }

    @Override
    public HealthStatus combine(@Nonnull final HealthStatus newStatus) {
        return newStatus;
    }

    @Override
    public int compareTo(@Nonnull final HealthStatus other) {
        return other instanceof OkStatus ? 0 : -1;
    }

    @Override
    public String toString(final Locale locale) {
        return "Everything is fine";
    }
}
