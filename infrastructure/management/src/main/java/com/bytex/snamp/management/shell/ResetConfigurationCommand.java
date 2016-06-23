package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.*;
import org.apache.karaf.shell.commands.Command;

/**
 * Resets SNAMP configuration.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "reset-config",
    description = "Reset configuration to empty")
public final class ResetConfigurationCommand extends ConfigurationCommand {

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        configuration.getEntities(ManagedResourceConfiguration.class).clear();
        configuration.getEntities(ResourceAdapterConfiguration.class).clear();
        return true;
    }
}
