package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ResourceAdapterConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Registers a new adapter instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "configure-adapter",
    description = "Configure new or existing adapter instance")
public final class ConfigAdapterInstanceCommand extends ConfigurationCommand<ResourceAdapterConfiguration> {
    @Argument(name = "instanceName", index = 0, required = true, description = "Name of the adapter instance")
    @SpecialUse
    private String instanceName = "";

    @Argument(name = "systemName", index = 1, required = false, description = "System name of the adapter")
    @SpecialUse
    private String systemName = "";

    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, required = false, description = "Configuration parameters in the form of key=value")
    @SpecialUse
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    public ConfigAdapterInstanceCommand(){
        super(ResourceAdapterConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ResourceAdapterConfiguration> configuration, final StringBuilder output) {
        if (isNullOrEmpty(instanceName)) return false;
        final ResourceAdapterConfiguration adapter = configuration.getOrAdd(instanceName);
        //setup system name
        if (!isNullOrEmpty(systemName))
            adapter.setAdapterName(systemName);
        //setup parameters
        if (!ArrayUtils.isNullOrEmpty(parameters))
            for (final String pair : parameters) {
                final StringKeyValue keyValue = StringKeyValue.parse(pair);
                if (keyValue != null)
                    adapter.getParameters().put(keyValue.getKey(), keyValue.getValue());
            }
        output.append("Updated");
        return true;
    }
}
