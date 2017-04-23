package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.connector.health.ResourceMalfunctionStatus;
import org.openstack4j.model.senlin.Node;

import java.util.Locale;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ProblemWithClusterNode extends ResourceMalfunctionStatus {
    private static final long serialVersionUID = -5029802853159238367L;
    private final String details;
    private final boolean critical;

    private ProblemWithClusterNode(final String resourceName,
                           final String details,
                                   final boolean critical) {
        super(resourceName);
        this.critical = critical;
        if(isNullOrEmpty(details))
            this.details = critical ? "Node is not available" : "Node is in non-critical state";
        else
            this.details = details;
    }

    static ProblemWithClusterNode error(final Node node){
        return new ProblemWithClusterNode(node.getName(), node.getStatusReason(), true);
    }

    static ProblemWithClusterNode migrating(final Node node) {
        return new ProblemWithClusterNode(node.getName(), "The server is currently being migrated", true);
    }

    static ProblemWithClusterNode reboot(final Node node, final boolean hardReboot) {
        return new ProblemWithClusterNode(node.getName(),
                hardReboot ? "The server is hard rebooting" : "The server is in a soft reboot state. A reboot command was passed to the operating system",
                hardReboot);
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
     * Indicates that resource is in critical state (potentially unavailable).
     *
     * @return {@literal true}, if managed resource is in critical state; otherwise, {@literal false}.
     */
    @Override
    public boolean isCritical() {
        return critical;
    }
}
