package com.bytex.snamp.supervision.discovery;

import com.bytex.snamp.connector.ManagedResourceConnector;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ResourceDiscoveryService {
    ManagedResourceConnector createResource(final String resourceName, final String connectionString);

}
