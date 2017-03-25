package com.bytex.snamp.configuration.internal;


import com.bytex.snamp.configuration.ManagedResourceConfiguration;

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
public interface CMManagedResourceParser extends CMRootEntityParser<ManagedResourceConfiguration> {
    /**
     * Extracts resource name from the managed resource configuration.
     *
     * @param resourceConfig A dictionary that represents managed resource configuration.
     * @return The resource name.
     */
    String getResourceName(final Dictionary<String, ?> resourceConfig);
}
