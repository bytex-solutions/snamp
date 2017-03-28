package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.annotation.Nonnull;

import static com.bytex.snamp.management.ManagementUtils.appendln;

/**
 * Displays configuration of gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "gateway-instance",
    description = "Display configuration of gateway instance")
@Service
public final class GatewayInstanceInfoCommand extends ConfigurationCommand<GatewayConfiguration> {
    @Argument(index = 0, name = "instanceName", required = true, description = "Name of gateway instance to display")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String instanceName = "";

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final StringBuilder output) {
        if (configuration.containsKey(instanceName)) {
            final GatewayConfiguration gatewayInstanceConfig = configuration.get(instanceName);
            appendln(output, "Instance Name: %s", instanceName);
            appendln(output, "System Name: %s", gatewayInstanceConfig.getType());
            appendln(output, "Configuration parameters:");
            gatewayInstanceConfig.forEach((key, value) -> appendln(output, "%s = %s", key, value));
        } else
            output.append("Gateway instance doesn't exist");
        return false;
    }

    @Nonnull
    @Override
    public EntityMap<? extends GatewayConfiguration> apply(@Nonnull final AgentConfiguration owner) {
        return owner.getGateways();
    }
}
