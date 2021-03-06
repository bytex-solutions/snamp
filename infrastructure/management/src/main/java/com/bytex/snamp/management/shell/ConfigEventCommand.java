package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.ManagedResourceTemplate;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.util.Arrays;


/**
 * Configures resource event.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
    name = "configure-event",
    description = "Configure resource event")
@Service
public final class ConfigEventCommand extends TemplateConfigurationCommand {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "name", required = true, description = "Name of the resource or group of resources")
    private String name = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 1, name = "category", required = true, description = "Category of the event")
    private String category = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-g", aliases = {"--group"}, description = "Configure group instead of resource")
    private boolean useGroup = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete event from resource or group")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Event configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-dp", aliases = {"--delete-parameter"}, multiValued = true, description = "Configuration parameters to be deleted")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parametersToDelete = parameters;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-o", aliases = "--override", description = "Override configuration of event declared by group")
    private boolean override;

    private boolean processEvents(final EntityMap<? extends EventConfiguration> events, final PrintWriter output) {
        if (del)
            events.remove(category);
        else {
            final EventConfiguration event = events.getOrAdd(category);
            event.putAll(StringKeyValue.parse(parameters));
            event.setOverridden(override);
            Arrays.stream(parametersToDelete).forEach(event::remove);
        }
        output.println("Event configured successfully");
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceTemplate> configuration, final PrintWriter output) {
        if (configuration.containsKey(name))
            return processEvents(configuration.get(name).getEvents(), output);
        else {
            output.println("Resource doesn't exist");
            return false;
        }
    }

    @Nonnull
    @Override
    public EntityMap<? extends ManagedResourceTemplate> apply(@Nonnull final AgentConfiguration owner) {
        return useGroup ? owner.getResourceGroups() : owner.getResources();
    }
}
