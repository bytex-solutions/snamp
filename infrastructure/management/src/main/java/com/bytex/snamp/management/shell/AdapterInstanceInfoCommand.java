package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import java.util.Map;

import com.bytex.snamp.configuration.ResourceAdapterConfiguration;
import static com.bytex.snamp.management.shell.Utils.appendln;

/*
 * Displays configuration of adapter instance.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "adapter",
    description = "Display configuration of adapter instance")
public final class AdapterInstanceInfoCommand extends ConfigurationCommand {
    @Argument(index = 0, name = "adapterInstance", required = true, description = "Name of adapter instance to display")
    @SpecialUse
    private String adapterInstance = "";

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if (configuration.getEntities(ResourceAdapterConfiguration.class).containsKey(adapterInstance)) {
            final ResourceAdapterConfiguration adapter = configuration.getEntities(ResourceAdapterConfiguration.class).get(adapterInstance);
            appendln(output, "Instance Name: %s", adapterInstance);
            appendln(output, "System Name: %s", adapter.getAdapterName());
            appendln(output, "Configuration parameters:");
            for (final Map.Entry<String, String> pair : adapter.getParameters().entrySet())
                appendln(output, "%s = %s", pair.getKey(), pair.getValue());
        } else
            output.append("Adapter instance doesn't exist");
        return false;
    }
}
