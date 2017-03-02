package com.bytex.snamp.connector.supervision;

import com.bytex.snamp.Localizable;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents health check status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class HealthStatus implements Serializable, Comparable<HealthStatus>, Localizable {
    private static final long serialVersionUID = -8700097915541124870L;
    private final int code;
    private final String resourceName;

    HealthStatus(final String resourceName, final int statusCode) {
        code = statusCode;
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    /**
     * Gets the most problematic resource.
     *
     * @return The most problematic resource. Can be empty if status is {@link OkStatus}.
     */
    public final String getResourceName() {
        return resourceName;
    }

    public abstract boolean isCritical();

    public final int getStatusCode() {
        return code;
    }

    @Override
    public String toString() {
        return toString(Locale.getDefault());
    }

    public HealthStatus combine(@Nonnull final HealthStatus newStatus) {
        if (compareTo(newStatus) < 0)
            return newStatus;
        else if (newStatus.getResourceName().equals(resourceName))
            return compareTo(newStatus) < 0 ? newStatus : this;
        else
            return this;
    }
}
