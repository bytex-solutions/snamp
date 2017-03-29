package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class ManagedResourceConfigurationCommand extends ConfigurationCommand<ManagedResourceConfiguration> {
    ManagedResourceConfigurationCommand() {
        super(AgentConfiguration::getResources);
    }
}
