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

/**
 * Displays configuration of the managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
        name = "resource",
        description = "Display configuration of the managed resource")
@Service
public final class ResourceInfoCommand extends ManagedResourceConfigurationCommand {
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of configured resource to display")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String resourceName = "";

    @Option(name = "-a", aliases = {"--attributes"}, description = "Show resource attributes", required = false, multiValued = false)
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showAttributes = false;

    @Option(name = "-e", aliases = {"--events"}, description = "Show resource events", required = false, multiValued = false)
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showEvents = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-o", aliases = {"--operations"}, description = "Show resource operations", required = false, multiValued = false)
    private boolean showOperations = false;

    static void printAttribute(final String userDefinedName, final AttributeConfiguration attr, final PrintWriter output){
        output.println(userDefinedName);
        output.format("Read/write Timeout: %s", attr.getReadWriteTimeout()).println();
        printParameters(attr, output);
    }

    static void printEvent(final String userDefinedName, final EventConfiguration ev, final PrintWriter output){
        output.println(userDefinedName);
        printParameters(ev, output);
    }

    static void printOperation(final String userDefinedName, final OperationConfiguration op, final PrintWriter output){
        output.println(userDefinedName);
        output.format("Invocation Timeout: %s", op.getInvocationTimeout()).println();
        printParameters(op, output);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final PrintWriter output) throws InterruptedException {
        if (configuration.containsKey(resourceName)) {
            final ManagedResourceConfiguration resource = configuration.get(resourceName);
            output.format("Resource Name: %s", resourceName).println();
            output.format("Connection Type: %s", resource.getType()).println();
            output.format("Connection String: %s", resource.getConnectionString()).println();
            output.println("Configuration parameters:");
            printParameters(resource, output);
            checkInterrupted();
            if(showAttributes) {
                output.println("==ATTRIBUTES==");
                for (final Map.Entry<String, ? extends AttributeConfiguration> attr : resource.getAttributes().entrySet())
                    printAttribute(attr.getKey(), attr.getValue(), output);
                output.println();
            }
            checkInterrupted();
            if(showEvents){
                output.println("==EVENTS==");
                for (final Map.Entry<String, ? extends EventConfiguration> attr : resource.getEvents().entrySet())
                    printEvent(attr.getKey(), attr.getValue(), output);
                output.println();
            }
            checkInterrupted();
            if(showOperations){
                output.println("==OPERATIONS==");
                for (final Map.Entry<String, ? extends OperationConfiguration> attr : resource.getOperations().entrySet())
                    printOperation(attr.getKey(), attr.getValue(), output);
                output.println();
            }
        } else
            output.append("Resource doesn't exist");
        return false;
    }
}
