package com.bytex.snamp.connector.health;

import java.util.Locale;

/**
 * Indicates that the cluster is in recovery state and may be unavailable.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ClusterRecoveryStatus extends ClusterMalfunctionStatus {
    private static final long serialVersionUID = 3186397258235973954L;

    public ClusterRecoveryStatus(final String clusterName) {
        super(SEVERITY - 1, clusterName);
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
        return "Cluster is in recovery state and may be unavailable";
    }

    /**
     * Indicates that resource is in critical state (potentially unavailable).
     *
     * @return {@literal true}, if managed resource is in critical state; otherwise, {@literal false}.
     */
    @Override
    public boolean isCritical() {
        return false;
    }
}
