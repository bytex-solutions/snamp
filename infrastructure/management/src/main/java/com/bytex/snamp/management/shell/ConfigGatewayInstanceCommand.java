package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.internal.Utils;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;
import java.util.Arrays;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Registers a new gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
    name = "configure-gateway",
    description = "Configure new or existing gateway instance")
@Service
public final class ConfigGatewayInstanceCommand extends GatewayConfigurationCommand {
    @Argument(name = "instanceName", index = 0, required = true, description = "Name of the gateway instance")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String instanceName = "";

    @Argument(name = "type", index = 1, required = false, description = "Type of gateway")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String gatewayType = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete gateway")
    private boolean del = false;

    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, required = false, description = "Configuration parameters in the form of key=value")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-dp", aliases = {"--delete-parameter"}, multiValued = true, description = "Configuration parameters to be deleted")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parametersToDelete = parameters;

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> gateways, final PrintWriter output) {
        if (del)
            gateways.remove(instanceName);
        else {
            final GatewayConfiguration gateway = gateways.getOrAdd(instanceName);
            if (!isNullOrEmpty(gatewayType))
                gateway.setType(gatewayType);
            gateway.putAll(StringKeyValue.parse(parameters));
            Arrays.stream(parametersToDelete).forEach(gateway::remove);
        }
        output.println("Gateway configured successfully");
        return true;
    }
}
