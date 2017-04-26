package com.bytex.snamp.connector.health;

import javax.annotation.Nonnull;
import javax.management.JMException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Indicates that SNAMP resource connector throws unexpected exception.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ResourceConnectorMalfunction extends ResourceMalfunctionStatus {
    private static final long serialVersionUID = -1368848980168422995L;
    private final JMException error;
    private final String resourceName;

    public ResourceConnectorMalfunction(final String resourceName, final JMException e){
        super(resourceName, Instant.now());
        error = Objects.requireNonNull(e);
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    @Override
    @Nonnull
    public Level getLevel() {
        return Level.SEVERE;
    }

    /**
     * Gets exception that was thrown by resource connector.
     * @return Exception that was thrown by resource connector.
     */
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

    private boolean like(final ResourceConnectorMalfunction status){
        return super.like(status) && Objects.equals(error.getCause(), status.error.getCause());
    }

    @Override
    public boolean like(final HealthStatus status) {
        return status instanceof ResourceConnectorMalfunction && like((ResourceConnectorMalfunction) status);
    }
}
