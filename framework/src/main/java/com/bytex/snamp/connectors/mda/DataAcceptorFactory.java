package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.TimeSpan;

import java.util.Map;

/**
 * Represents a factory of {@link DataAcceptor} class.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface DataAcceptorFactory {
    /**
     * Creates a new instance of the Monitoring Data Acceptor.
     * @param resourceName The name of managed resource.
     * @param connectionString Initialization string.
     * @param expirationTime The period of time during which the monitoring data is available from the connector.
     * @param parameters Initialization parameters.
     * @return A new instance.
     * @throws Exception Unable to create acceptor.
     */
    DataAcceptor create(final String resourceName,
                        final String connectionString,
                        final TimeSpan expirationTime,
                        final Map<String, String> parameters) throws Exception;

    /**
     * Determines whether this factory can create Data Acceptor using the specified connection string.
     * @param connectionString Candidate connection string.
     * @return {@literal true}, if this factory can be used to produce a new instance of Data Acceptor
     *      using specified connection string; otherwise, {@literal false}.
     */
    boolean canCreateFrom(final String connectionString);
}
