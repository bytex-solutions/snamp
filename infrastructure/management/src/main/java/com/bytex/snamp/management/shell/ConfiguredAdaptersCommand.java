package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Command;

import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.bytex.snamp.io.IOUtils.appendln;

/**
 * Prints list of configured adapter instances.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "adapter-instances",
        description = "List of configured adapter instances")
public final class ConfiguredAdaptersCommand extends ConfigurationCommand {
    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        for(final Map.Entry<String, ? extends ResourceAdapterConfiguration> adapter: configuration.getResourceAdapters().entrySet())
            appendln(output, "Instance: %s. Adapter: %s", adapter.getKey(), adapter.getValue().getAdapterName());
        return false;
    }
}
