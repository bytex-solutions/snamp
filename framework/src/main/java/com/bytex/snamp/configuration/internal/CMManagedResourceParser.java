package com.bytex.snamp.configuration.internal;


import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

import java.io.IOException;
import java.util.Dictionary;

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

    @Override
    SingletonMap<String, ? extends ManagedResourceConfiguration> parse(final Dictionary<String, ?> config) throws IOException;

    /**
     * Extracts resource name from the managed resource configuration.
     * @param resourceConfig A dictionary that represents managed resource configuration.
     * @return The resource name.
     */
    String getResourceName(final Dictionary<String, ?> resourceConfig);

}
