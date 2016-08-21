package com.bytex.snamp.connector;

import com.bytex.snamp.core.SupportService;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Represents support service allows to create a new instances of {@link ManagedResourceConnector}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ManagedResourceConnectorFactoryService extends SupportService {
    /**
     * Represents instantiation parameter of type {@link String} containing resource connection string.
     */
    String CONNECTION_STRING = "connectionString";
    /**
     * Represents instantiation parameter of type {@link String} containing resource name.
     */
    String RESOURCE_NAME = "resourceName";
    /**
     * Represents instantiation parameter of type {@link Map}&lt{@link String}, {@link String}&gt; containing a set of connection parameters.
     */
    String CONNECTION_PARAMS = "connectionParams";

    /**
     * Creates a new instance of {@link ManagedResourceConnector} using supplied parameters.
     * @param parameters Read-only map of parameters used to instantiate a new instance of {@link ManagedResourceConnector}
     * @return A new instance of {@link ManagedResourceConnector}.
     * @throws Exception An exception occurred by {@link ManagedResourceConnector} constructor.
     * @throws InstantiationException Not enough parameters to instantiate {@link ManagedResourceConnector}.
     * @see #CONNECTION_PARAMS
     * @see #CONNECTION_STRING
     * @see #RESOURCE_NAME
     */
    ManagedResourceConnector createConnector(final Map<String, ?> parameters) throws Exception;

    static Map<String, ?> instantiationParameters(final String connectionString,
                                                       final String resourceName,
                                                       final Map<String, String> connectionParameters){
        return ImmutableMap.of(
            CONNECTION_STRING, connectionString,
            RESOURCE_NAME, resourceName,
            CONNECTION_PARAMS, connectionParameters
        );
    }
}
