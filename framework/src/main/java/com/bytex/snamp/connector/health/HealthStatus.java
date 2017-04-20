package com.bytex.snamp.connector.health;

import com.bytex.snamp.Localizable;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.emptyToNull;

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
    private HashMap<String, Object> data;

    HealthStatus(final String resourceName, final int statusCode) {
        code = statusCode;
        this.resourceName = resourceName;
    }

    public final void putData(@Nonnull final String name, @Nonnull final Object value){
        if(data == null)
            data = new HashMap<>();
        data.put(name, value);
    }

    public final void putData(@Nonnull final Map<String, ?> value) {
        if (data == null)
            data = new HashMap<>(value);
        else
            data.putAll(value);
    }

    /**
     * Gets additional data associated with this status.
     * @return Immutable map with additional data associated with this instance.
     */
    public final Map<String, ?> getData(){
        return firstNonNull(data, ImmutableMap.of());
    }

    /**
     * Gets the most problematic resource.
     *
     * @return The most problematic resource. Can be empty if status is {@link OkStatus}.
     */
    public final Optional<String> getResourceName() {
        return Optional.ofNullable(emptyToNull(resourceName));
    }

    public abstract boolean isCritical();

    public final int getStatusCode() {
        return code;
    }

    public final HealthStatus worst(@Nonnull final HealthStatus other){
        return compareTo(other) < 0 ? other : this;
    }

    @Override
    public String toString() {
        return toString(Locale.getDefault());
    }
}
