package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.ResourceMalfunctionStatus;
import org.openstack4j.model.senlin.Node;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Locale;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class ProblemWithClusterNode extends ResourceMalfunctionStatus {
    private static final long serialVersionUID = -5029802853159238367L;
    private final String details;
    private final Level level;

    private ProblemWithClusterNode(final String details,
                                   final Level level) {
        super(Instant.now());
        this.level = level;
        this.details = nullToEmpty(details);
    }

    static ProblemWithClusterNode error(final Node node){
        return new ProblemWithClusterNode(node.getStatusReason(), Level.SEVERE);
    }

    static ProblemWithClusterNode migrating(final Node node) {
        return new ProblemWithClusterNode("The server is currently being migrated", Level.MODERATE);
    }

    static ProblemWithClusterNode reboot(final Node node, final boolean hardReboot) {
        return new ProblemWithClusterNode(
                hardReboot ? "The server is hard rebooting" : "The server is in a soft reboot state. A reboot command was passed to the operating system",
                hardReboot ? Level.SUBSTANTIAL : Level.MODERATE);
    }

    static ProblemWithClusterNode shutoff(final Node node) {
        return new ProblemWithClusterNode("The server was powered down by the user, but not through the OpenStack Compute API.", Level.MODERATE);
    }

    static ProblemWithClusterNode paused(final Node node) {
        return new ProblemWithClusterNode("The server is in frozen state.", Level.MODERATE);
    }

    static ProblemWithClusterNode resize(final Node node) {
        return new ProblemWithClusterNode("Server is performing the differential copy of data that changed during its initial copy.", Level.LOW);
    }

    static ProblemWithClusterNode suspended(final Node node) {
        return new ProblemWithClusterNode("Server state is stored on disk, all memory is written to disk, and the server is stopped.", Level.MODERATE);
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
        return details;
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

    private boolean like(final ProblemWithClusterNode status){
        return super.like(status) && status.details.equals(details);
    }

    @Override
    public boolean like(final HealthStatus status) {
        return status instanceof ProblemWithClusterNode && like((ProblemWithClusterNode) status);
    }
}
