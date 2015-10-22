package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.google.common.base.Strings;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * Registers a new adapter instance.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "configure-adapter",
    description = "Configures new or existing adapter instance")
public final class ConfigAdapterInstanceCommand extends ConfigurationCommand {
    @Argument(name = "instanceName", index = 0, required = true, description = "Name of the adapter instance")
    @SpecialUse
    private String instanceName = "";

    @Argument(name = "systemName", index = 1, required = false, description = "System name of the adapter")
    @SpecialUse
    private String systemName = "";

    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, required = false, description = "Configuration parameters in the form of key=value")
    @SpecialUse
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(Strings.isNullOrEmpty(instanceName)) return false;
        final ResourceAdapterConfiguration adapter;
        if(configuration.getResourceAdapters().containsKey(instanceName)){//modify existing adapter
            adapter = configuration.getResourceAdapters().get(instanceName);
            output.append("Updated");
        }
        else {  //create new adapter instance
            adapter = configuration.newConfigurationEntity(ResourceAdapterConfiguration.class);
            configuration.getResourceAdapters().put(instanceName, adapter);
            output.append("Created");
        }
        //setup system name
        if(!Strings.isNullOrEmpty(systemName))
            adapter.setAdapterName(systemName);
        //setup parameters
        if(!ArrayUtils.isNullOrEmpty(parameters))
            for(final String pair: parameters){
                final StringKeyValue keyValue = StringKeyValue.parse(pair);
                adapter.getParameters().put(keyValue.getKey(), keyValue.getValue());
            }
        return true;
    }
}
