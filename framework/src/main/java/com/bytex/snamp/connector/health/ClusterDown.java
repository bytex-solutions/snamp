package com.bytex.snamp.connector.health;

import java.util.Locale;

/**
 * Indicates that cluster is unavailable or partially unavailable.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ClusterDown extends ClusterMalfunction {
    public static final int CODE = 4;
    private static final long serialVersionUID = 1553458150714807415L;

    public ClusterDown(final String clusterName, final boolean critical){
        super(clusterName, CODE, critical);
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
        if(isCritical())
            return String.format("Cluster %s is unavailable", getClusterName());
        else
            return String.format("Cluster %s is partially unavailable", getClusterName());
    }
}
