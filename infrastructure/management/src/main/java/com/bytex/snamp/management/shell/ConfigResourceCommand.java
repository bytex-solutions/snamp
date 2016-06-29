package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.management.shell.Utils.appendln;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Configures managed resource.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "configure-resource",
    description = "Configure managed resource")
public final class ConfigResourceCommand extends ConfigurationCommand {
    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of the resource")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "connectionType", required = false, description = "Name of the connector")
    private String connectionType = "";

    @SpecialUse
    @Argument(index = 2, name = "connectionString", required = false, description = "Connection string used for connection with managed resource")
    private String connectionString = "";

    @SpecialUse
    @Option(name = "-p", aliases = {"-param, --parameter"}, required = false, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(isNullOrEmpty(resourceName)) return false;
        final ManagedResourceConfiguration resource = configuration.getOrRegisterEntity(ManagedResourceConfiguration.class, resourceName);
        //setup connection type
        if(!isNullOrEmpty(connectionType))
            resource.setConnectionType(connectionType);
        //setup connection string
        if(!isNullOrEmpty(connectionString))
            resource.setConnectionString(connectionString);
        //setup parameters
        if(!ArrayUtils.isNullOrEmpty(parameters))
            for(final String pair: parameters) {
                final StringKeyValue keyValue = StringKeyValue.parse(pair);
                if (keyValue != null)
                    resource.getParameters().put(keyValue.getKey(), keyValue.getValue());
            }
        appendln(output, "Updated");
        return true;
    }
}
