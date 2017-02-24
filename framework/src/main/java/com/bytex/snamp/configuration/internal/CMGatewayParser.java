package com.bytex.snamp.configuration.internal;

import com.bytex.snamp.SingletonCollection;
import com.bytex.snamp.configuration.GatewayConfiguration;

import java.util.Dictionary;

/**
 * Provides parsing of gateway configuration from data provided by {@link org.osgi.service.cm.Configuration}.
 * <p>
 *     This interface is intended to use from your code directly. Any future release of SNAMP may change
 *     configuration storage provided and this interface will be deprecated.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public interface CMGatewayParser extends CMConfigurationParser<GatewayConfiguration> {
    /**
     * Returns persistent identifier of the specified gateway.
     * @param gatewayType The name of the gateway instance.
     * @return Persistent identifier.
     */
    String getFactoryPersistentID(final String gatewayType);

    @Override
    SingletonCollection<? extends GatewayConfiguration> parse(final Dictionary<String, ?> configuration);

    /**
     * Extracts the name of the gateway instance from its configuration.
     * @param gatewayInstanceConfig The gateway instance configuration supplied by {@link org.osgi.service.cm.Configuration} object.
     * @return Gateway instance name.
     */
    String getInstanceName(final Dictionary<String, ?> gatewayInstanceConfig);
}
