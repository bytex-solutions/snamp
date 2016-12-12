package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Configures resource event.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "configure-event",
    description = "Configure resource event")
@Service
public final class ConfigEventCommand extends ConfigurationCommand<ManagedResourceConfiguration> {
    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "category", required = true, description = "Category of the event")
    private String category = "";

    @SpecialUse
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Event configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    public ConfigEventCommand(){
        super(ManagedResourceConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final StringBuilder output) {
        if(configuration.containsKey(resourceName)){
            final ManagedResourceConfiguration resource = configuration.get(resourceName);
            final EventConfiguration event = resource.getFeatures(EventConfiguration.class).getOrAdd(category);
            if(!ArrayUtils.isNullOrEmpty(parameters))
                for(final String param: parameters) {
                    final StringKeyValue pair = StringKeyValue.parse(param);
                    if (pair != null)
                        event.getParameters().put(pair.getKey(), pair.getValue());
                }
            output.append("Attribute configured successfully");
            return true;
        }
        else {
            output.append("Resource doesn't exist");
            return false;
        }
    }
}
