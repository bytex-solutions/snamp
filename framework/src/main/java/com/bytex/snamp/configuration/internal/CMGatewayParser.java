package com.bytex.snamp.configuration.internal;

import com.bytex.snamp.Internal;
import com.bytex.snamp.configuration.GatewayConfiguration;

/**
 * Provides parsing of gateway configuration from data provided by {@link org.osgi.service.cm.Configuration}.
 * <p>
 *     This interface is intended to use from your code directly. Any future release of SNAMP may change
 *     configuration storage provided and this interface will be deprecated.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
@Internal
public interface CMGatewayParser extends CMRootEntityParser<GatewayConfiguration> {
}
