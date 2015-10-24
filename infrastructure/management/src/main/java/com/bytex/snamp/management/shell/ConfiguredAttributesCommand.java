package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Shows list of resource attributes.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "attributes",
    description = "List of configured attributes assigned to the resource")
public final class ConfiguredAttributesCommand extends ConfigurationCommand {
    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of resource to explore")
    private String resourceName = "";


    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getManagedResources().containsKey(resourceName)) {
            for (final Map.Entry<String, AttributeConfiguration> entry : configuration.getManagedResources().get(resourceName).getElements(AttributeConfiguration.class).entrySet()) {
                IOUtils.appendln(output, "UserDefinedName: %s, Name: %s", entry.getKey(), entry.getValue().getAttributeName());
            }
            return true;
        }
        else {
            output.append("Resource doesn't exist");
            return false;
        }
    }
}
