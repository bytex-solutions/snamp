package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import java.util.Map;

import static com.bytex.snamp.management.shell.Utils.appendln;

/**
 * Displays configuration of adapter instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "gateway-instance",
    description = "Display configuration of adapter instance")
public final class GatewayInstanceInfoCommand extends ConfigurationCommand<GatewayConfiguration> {
    @Argument(index = 0, name = "instanceName", required = true, description = "Name of gateway instance to display")
    @SpecialUse
    private String instanceName = "";

    public GatewayInstanceInfoCommand(){
        super(GatewayConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final StringBuilder output) {
        if (configuration.containsKey(instanceName)) {
            final GatewayConfiguration adapter = configuration.get(instanceName);
            appendln(output, "Instance Name: %s", instanceName);
            appendln(output, "System Name: %s", adapter.getType());
            appendln(output, "Configuration parameters:");
            for (final Map.Entry<String, String> pair : adapter.getParameters().entrySet())
                appendln(output, "%s = %s", pair.getKey(), pair.getValue());
        } else
            output.append("Gateway instance doesn't exist");
        return false;
    }
}
