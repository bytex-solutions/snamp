package com.bytex.snamp.connector.health;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Indicates that a subsystem in the remote component is down.
 * <p>
 *     Subsystem of some service may be represented as connected distributed cache, database, storage etc.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ResourceSubsystemDownStatus extends ResourceMalfunctionStatus {
    private static final long serialVersionUID = 2509053676736114143L;
    private final String name;
    private final Level level;

    public ResourceSubsystemDownStatus(final Instant timeStamp, @Nonnull final String subsystem, final Level level) {
        super(timeStamp);
        name = Objects.requireNonNull(subsystem);
        this.level = Objects.requireNonNull(level);
    }

    public ResourceSubsystemDownStatus(final Instant timeStamp, @Nonnull final String subsystem){
        this(timeStamp, subsystem, Level.SEVERE);
    }

    /**
     * Gets name of broken subsystem.
     * @return Name of broken subsystem.
     */
    public String getSubsystem(){
        return name;
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
        return String.format("Subsystem %s is down", name);
    }

    private boolean like(final ResourceSubsystemDownStatus status) {
        return super.like(status) && status.getSubsystem().equals(getSubsystem());
    }

    /**
     * Determines whether this health status is similar to the specified status.
     *
     * @param status Health status.
     * @return {@literal true}, if this health status is similar to the specified status.
     * @implSpec This method has weaker semantics than {@link #equals(Object)}.
     * Similarity means that only significant data in health status used are equal.
     * Volatile data such as {@link #getTimeStamp()} should be ignored.
     */
    @Override
    public boolean like(final HealthStatus status) {
        return status instanceof ResourceSubsystemDownStatus && like((ResourceSubsystemDownStatus) status);
    }

    /**
     * Gets malfunction level.
     *
     * @return Malfunction level.
     */
    @Nonnull
    @Override
    public Level getLevel() {
        return level;
    }
}
