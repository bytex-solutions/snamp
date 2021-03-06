package com.bytex.snamp.connector;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.core.SupportService;

/**
 * Represents support service allows to create a new instances of {@link ManagedResourceConnector}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ManagedResourceConnectorFactoryService extends SupportService {
    /**
     * Creates a new instance of {@link ManagedResourceConnector} using supplied parameters.
     * @param resourceName Name of the managed resource.
     * @param configuration Configuration of the managed resource. Cannot be {@literal null}.
     * @return A new instance of {@link ManagedResourceConnector}.
     * @throws Exception An exception occurred by {@link ManagedResourceConnector} constructor.
     * @throws InstantiationException Not enough parameters to instantiate {@link ManagedResourceConnector}.
     */
    ManagedResourceConnector createConnector(final String resourceName, final ManagedResourceInfo configuration) throws Exception;
}
