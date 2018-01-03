package com.bytex.snamp.connector;

import com.bytex.snamp.core.SupportService;

import java.util.Map;

/**
 * Represents support service allows to create a new instances of {@link ManagedResourceConnector}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface ManagedResourceConnectorFactoryService extends SupportService {
    /**
     * Creates a new instance of {@link ManagedResourceConnector} using supplied parameters.
     * @param resourceName Name of the managed resource.
     * @param connectionString Connection string used to establish connection with managed resource.
     * @param parameters Connection parameters.
     * @return A new instance of {@link ManagedResourceConnector}.
     * @throws Exception An exception occurred by {@link ManagedResourceConnector} constructor.
     * @throws InstantiationException Not enough parameters to instantiate {@link ManagedResourceConnector}.
     */
    ManagedResourceConnector createConnector(final String resourceName,
                                             final String connectionString,
                                             final Map<String, String> parameters) throws Exception;
}
