package com.bytex.snamp.connector.health;

import java.util.Locale;

/**
 * Indicates that the cluster is resizing.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ClusterResizingStatus extends ClusterMalfunctionStatus {
    private static final long serialVersionUID = 4665622610267812819L;

    public ClusterResizingStatus(final String clusterName) {
        super(SEVERITY - 2, clusterName);
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
        return "Cluster is resizing";
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
