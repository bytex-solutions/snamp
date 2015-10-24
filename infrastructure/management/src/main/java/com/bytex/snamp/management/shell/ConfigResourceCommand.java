package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import com.google.common.base.Strings;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * Configures managed resource.
 * @author Roman Sakno
 * @version 1.0
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
        if(Strings.isNullOrEmpty(resourceName)) return false;
        final ManagedResourceConfiguration resource;
        if(configuration.getManagedResources().containsKey(resourceName)){//modify existing resource
            resource = configuration.getManagedResources().get(resourceName);
            IOUtils.appendln(output, "Updated");
        }
        else {  //create new adapter instance
            resource = configuration.newConfigurationEntity(ManagedResourceConfiguration.class);
            configuration.getManagedResources().put(resourceName, resource);
            IOUtils.appendln(output, "Created");
        }
        //setup connection type
        if(!Strings.isNullOrEmpty(connectionType))
            resource.setConnectionType(connectionType);
        //setup connection string
        if(!Strings.isNullOrEmpty(connectionString))
            resource.setConnectionString(connectionString);
        //setup parameters
        if(!ArrayUtils.isNullOrEmpty(parameters))
            for(final String pair: parameters){
                final StringKeyValue keyValue = StringKeyValue.parse(pair);
                resource.getParameters().put(keyValue.getKey(), keyValue.getValue());
            }
        return true;
    }
}
