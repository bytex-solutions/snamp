package com.bytex.snamp.configuration.internal;

import com.bytex.snamp.configuration.GatewayConfiguration;

import java.util.Dictionary;
import java.util.Map;

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

    /**
     * Extracts the name of the gateway instance from its configuration.
     * @param gatewayInstanceConfig The gateway instance configuration supplied by {@link org.osgi.service.cm.Configuration} object.
     * @return Gateway instance name.
     */
    String getInstanceName(final Dictionary<String, ?> gatewayInstanceConfig);

    /**
     * Extracts configuration parameters of resource adapter.
     * @param gatewayInstanceConfig A collection of configuration parameters supplied by {@link org.osgi.service.cm.Configuration} object.
     * @return Configuration parameters of resource adapter.
     */
    Map<String, String> getParameters(final Dictionary<String, ?> gatewayInstanceConfig);
}
