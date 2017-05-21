package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.internal.Utils;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;
import java.util.Map;

import static com.bytex.snamp.management.shell.ResourceInfoCommand.*;
;
/**
 * Displays full information about managed resource group.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
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
    boolean doExecute(final EntityMap<? extends ManagedResourceGroupConfiguration> configuration, final PrintWriter output) throws InterruptedException {
        if (configuration.containsKey(groupName)) {
            final ManagedResourceGroupConfiguration group = configuration.get(groupName);
            output.format("Group Name: %s", groupName).println();
            output.format("Connection Type: %s", group.getType()).println();
            output.println("Configuration parameters:");
            printParameters(group, output);
            checkInterrupted();
            if(showAttributes) {
                output.println("==ATTRIBUTES==");
                for (final Map.Entry<String, ? extends AttributeConfiguration> attr : group.getAttributes().entrySet())
                    printAttribute(attr.getKey(), attr.getValue(), output);
                output.println();
            }
            checkInterrupted();
            if(showEvents){
                output.println("==EVENTS==");
                for (final Map.Entry<String, ? extends EventConfiguration> attr : group.getEvents().entrySet())
                    printEvent(attr.getKey(), attr.getValue(), output);
                output.println();
            }
            checkInterrupted();
            if(showOperations){
                output.println("==OPERATIONS==");
                for (final Map.Entry<String, ? extends OperationConfiguration> attr : group.getOperations().entrySet())
                    printOperation(attr.getKey(), attr.getValue(), output);
                output.println();
            }
        } else
            output.append("Resource group doesn't exist");
        return false;
    }
}
