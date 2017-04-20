package com.bytex.snamp.connector.health;

import javax.management.JMException;
import java.util.Locale;
import java.util.Objects;

/**
 * Indicates that SNAMP cannot obtain access to the managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ResourceIsNotAvailable extends ResourceMalfunctionStatus {
    private static final long serialVersionUID = -1368848980168422995L;
    private final JMException error;
    private final String resourceName;

    public ResourceIsNotAvailable(final String resourceName, final JMException e){
        super(resourceName, SEVERITY + 1);
        error = Objects.requireNonNull(e);
        this.resourceName = Objects.requireNonNull(resourceName);
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

    public JMException getError(){
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
        return String.format("Resource %s is not available. Caused by: %s", resourceName, error);
    }
}
