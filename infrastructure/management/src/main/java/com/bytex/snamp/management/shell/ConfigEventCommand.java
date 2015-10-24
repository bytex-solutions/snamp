package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.google.common.base.Strings;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Configures resource event.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "configure-event",
    description = "Configure resource event")
public final class ConfigEventCommand extends ConfigurationCommand {
    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "userDefinedName", required = true, description = "User-defined name of the event")
    private String userDefinedName = "";

    @SpecialUse
    @Argument(index = 2, name = "category", required = false, description = "Resource-specific category of the event")
    private String category = "";

    @SpecialUse
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Event configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getManagedResources().containsKey(resourceName)){
            final AgentConfiguration.ManagedResourceConfiguration resource = configuration.getManagedResources().get(resourceName);
            final EventConfiguration event;
            if(resource.getElements(EventConfiguration.class).containsKey(userDefinedName))
                event = resource.getElements(EventConfiguration.class).get(userDefinedName);
            else {
                event = resource.newElement(EventConfiguration.class);
                resource.getElements(EventConfiguration.class).put(userDefinedName, event);
            }
            if(!Strings.isNullOrEmpty(category))
                event.setCategory(category);
            if(!ArrayUtils.isNullOrEmpty(parameters))
                for(final String param: parameters) {
                    final StringKeyValue pair = StringKeyValue.parse(param);
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
