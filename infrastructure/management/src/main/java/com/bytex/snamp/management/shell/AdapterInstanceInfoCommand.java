package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * Displays configuration of adapter instance.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "adapter",
    description = "Display configuration of adapter instance")
public final class AdapterInstanceInfoCommand extends ConfigurationCommand {
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of adapter instance to display")
    @SpecialUse
    private String resourceName = "";

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if (configuration.getResourceAdapters().containsKey(resourceName)) {
            final ResourceAdapterConfiguration adapter = configuration.getResourceAdapters().get(resourceName);
            IOUtils.appendln(output, "Instance Name: %s", resourceName);
            IOUtils.appendln(output, "System Name: %s", adapter.getAdapterName());
            IOUtils.appendln(output, "Configuration parameters:");
            for (final Map.Entry<String, String> pair : adapter.getParameters().entrySet())
                IOUtils.appendln(output, "%s = %s", pair.getKey(), pair.getValue());
        } else
            output.append("Adapter instance doesn't exist");
        return false;
    }
}
