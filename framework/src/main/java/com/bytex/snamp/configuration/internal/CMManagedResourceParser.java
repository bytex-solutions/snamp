package com.bytex.snamp.configuration.internal;


import com.bytex.snamp.configuration.ManagedResourceConfiguration;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.*;

/**
 * Provides parsing of managed resource configuration from data provided by {@link org.osgi.service.cm.Configuration}.
 * <p>
 *     This interface is intended to use from your code directly. Any future release of SNAMP may change
 *     configuration storage provided and this interface will be deprecated.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public interface CMManagedResourceParser extends CMConfigurationParser<ManagedResourceConfiguration> {
    /**
     * Returns managed connector persistent identifier.
     * @param connectorType The type of the managed resource connector.
     * @return The persistent identifier.
     */
    String getFactoryPersistentID(final String connectorType);

    /**
     * Extracts resource connection string from the managed resource configuration.
     * @param resourceConfig A dictionary that represents managed resource configuration.
     * @return Resource connection string.
     */
    String getConnectionString(final Dictionary<String, ?> resourceConfig);

    /**
     * Extracts resource name from the managed resource configuration.
     * @param resourceConfig A dictionary that represents managed resource configuration.
     * @return The resource name.
     */
    String getResourceName(final Dictionary<String, ?> resourceConfig);

    Map<String, String> getParameters(final Dictionary<String, ?> resourceConfig);

    Map<String, ? extends AttributeConfiguration> getAttributes(final Dictionary<String, ?> resourceConfig) throws IOException;

    Map<String, ? extends OperationConfiguration> getOperations(final Dictionary<String, ?> resourceConfig) throws IOException;

    Map<String, ? extends EventConfiguration> getEvents(final Dictionary<String, ?> resourceConfig) throws IOException;

}
