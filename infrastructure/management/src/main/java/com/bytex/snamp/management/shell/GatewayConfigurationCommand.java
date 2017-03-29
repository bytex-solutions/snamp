package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class GatewayConfigurationCommand extends ConfigurationCommand<GatewayConfiguration> {
    GatewayConfigurationCommand() {
        super(AgentConfiguration::getGateways);
    }
}
