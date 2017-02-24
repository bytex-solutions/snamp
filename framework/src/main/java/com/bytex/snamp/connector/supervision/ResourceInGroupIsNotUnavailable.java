package com.bytex.snamp.connector.supervision;

import javax.management.JMException;
import java.util.Locale;
import java.util.Objects;

/**
 * Indicates that SNAMP cannot obtain access to the managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ResourceInGroupIsNotUnavailable extends MalfunctionStatus implements GroupStatus {
    public static final int CODE = 2;
    private static final long serialVersionUID = -1368848980168422995L;
    private final JMException error;
    private final String resourceName;

    public ResourceInGroupIsNotUnavailable(final String resourceName, final JMException e){
        super(CODE, true);
        error = Objects.requireNonNull(e);
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    public JMException getError(){
        return error;
    }

    @Override
    public int hashCode() {
        return error.hashCode();
    }

    private boolean equals(final ResourceInGroupIsNotUnavailable other){
        return Objects.equals(error, other.error);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ResourceInGroupIsNotUnavailable && equals((ResourceInGroupIsNotUnavailable) other);
    }

    /**
     * Gets the most problematic resource.
     *
     * @return The most problematic resource. Can be empty if status is {@link OkStatus}.
     */
    @Override
    public final String getResourceName() {
        return resourceName;
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
        return String.format("Resource %s is not available. Caused by: %s", resourceName, error);
    }
}
