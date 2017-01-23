package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Registers a new gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "configure-gateway",
    description = "Configure new or existing gateway instance")
@Service
public final class ConfigGatewayInstanceCommand extends ConfigurationCommand<GatewayConfiguration> {
    @Argument(name = "instanceName", index = 0, required = true, description = "Name of the gateway instance")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String instanceName = "";

    @Argument(name = "type", index = 1, required = false, description = "Type of gateway")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String gatewayType = "";

    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, required = false, description = "Configuration parameters in the form of key=value")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    public ConfigGatewayInstanceCommand(){
        super(GatewayConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final StringBuilder output) {
        if (isNullOrEmpty(instanceName)) return false;
        final GatewayConfiguration gatewayInstanceConfig = configuration.getOrAdd(instanceName);
        //setup system name
        if (!isNullOrEmpty(gatewayType))
            gatewayInstanceConfig.setType(gatewayType);
        //setup parameters
        if (!ArrayUtils.isNullOrEmpty(parameters))
            for (final String pair : parameters) {
                final StringKeyValue keyValue = StringKeyValue.parse(pair);
                if (keyValue != null)
                    gatewayInstanceConfig.put(keyValue.getKey(), keyValue.getValue());
            }
        output.append("Updated");
        return true;
    }
}
