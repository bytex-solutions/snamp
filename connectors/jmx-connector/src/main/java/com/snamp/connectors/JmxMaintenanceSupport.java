package com.snamp.connectors;

import com.snamp.Internal;

/**
 * Provides additional subroutines for managing the JMX connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public interface JmxMaintenanceSupport {
    /**
     * Simulates JMX connection abort and forces re-connect.
     */
    @Internal
    public void simulateConnectionAbort();
}
