package com.bytex.snamp.connector.health;

import java.util.Locale;

/**
 * Indicates that cluster recovering and MAY BE unavailable.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ClusterRecovering extends ClusterMalfunction {
    public static final int CODE = 4;
    private static final long serialVersionUID = 2582624192520381081L;

    public ClusterRecovering(final String clusterName) {
        super(clusterName, 4, false);
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
        return null;
    }
}
