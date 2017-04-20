package com.bytex.snamp.connector.health;

import javax.annotation.Nonnull;

/**
 * Something wrong with managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class MalfunctionStatus extends HealthStatus {
    private static final long serialVersionUID = -1766747580186741189L;
    private final boolean critical;

    protected MalfunctionStatus(final String resourceName,
                                final int statusCode,
                                final boolean critical) {
        super(resourceName, statusCode);
        this.critical = critical;
    }

    /**
     * Indicates that managed resource is in critical state (potentially unavailable).
     * @return {@literal true}, if managed resource is in critical state; otherwise, {@literal false}.
     */
    @Override
    public boolean isCritical() {
        return critical;
    }

    @Override
    public int compareTo(@Nonnull final HealthStatus other) {
        switch (other.getStatusCode()) {
            case OkStatus.CODE:
                return 1;
            default:
                return Boolean.compare(isCritical(), other.isCritical());
        }
    }
}
