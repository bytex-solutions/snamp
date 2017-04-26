package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.util.Map;

import static com.bytex.snamp.management.ManagementUtils.appendln;
import static com.bytex.snamp.management.ManagementUtils.newLine;
import static com.bytex.snamp.management.shell.ResourceInfoCommand.*;
;
/**
 * Displays full information about managed resource group.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "resource-group",
        description = "Display configuration of the managed resource group")
@Service
public final class GroupInfoCommand extends GroupConfigurationCommand {
    @Argument(index = 0, name = "groupName", required = true, description = "Name of configured resource group to display")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String groupName = "";

    @Option(name = "-a", aliases = {"--attributes"}, description = "Show resource attributes", required = false, multiValued = false)
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showAttributes = false;

    @Option(name = "-e", aliases = {"--events"}, description = "Show resource events", required = false, multiValued = false)
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showEvents = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-o", aliases = {"--operations"}, description = "Show resource operations", required = false, multiValued = false)
    private boolean showOperations = false;

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceGroupConfiguration> configuration, final StringBuilder output) throws InterruptedException {
        if (configuration.containsKey(groupName)) {
            final ManagedResourceGroupConfiguration group = configuration.get(groupName);
            appendln(output, "Group Name: %s", groupName);
            appendln(output, "Connection Type: %s", group.getType());
            appendln(output, "Configuration parameters:");
            group.forEach((key, value) -> appendln(output, "%s = %s", key, value));
            checkInterrupted();
            if(showAttributes) {
                appendln(output, "==ATTRIBUTES==");
                for (final Map.Entry<String, ? extends AttributeConfiguration> attr : group.getAttributes().entrySet())
                    printAttribute(attr.getKey(), attr.getValue(), output);
                newLine(output);
            }
            checkInterrupted();
            if(showEvents){
                appendln(output, "==EVENTS==");
                for (final Map.Entry<String, ? extends EventConfiguration> attr : group.getEvents().entrySet())
                    printEvent(attr.getKey(), attr.getValue(), output);
                newLine(output);
            }
            checkInterrupted();
            if(showOperations){
                appendln(output, "==OPERATIONS==");
                for (final Map.Entry<String, ? extends OperationConfiguration> attr : group.getOperations().entrySet())
                    printOperation(attr.getKey(), attr.getValue(), output);
                newLine(output);
            }
        } else
            output.append("Resource group doesn't exist");
        return false;
    }
}
