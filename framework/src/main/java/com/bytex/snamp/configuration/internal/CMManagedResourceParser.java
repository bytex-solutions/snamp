package com.bytex.snamp.configuration.internal;


import com.bytex.snamp.Internal;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

/**
 * Provides parsing of managed resource configuration from data provided by {@link org.osgi.service.cm.Configuration}.
 * <p>
 *     This interface is intended to use from your code directly. Any future release of SNAMP may change
 *     configuration storage provided and this interface will be deprecated.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.2
 */
@Internal
public interface CMManagedResourceParser extends CMRootEntityParser<ManagedResourceConfiguration> {
}
