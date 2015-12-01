package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

/**
 * Displays configuration of the managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "resource",
    description = "Display configuration of the managed resource")
public final class ResourceInfoCommand extends ConfigurationCommand {
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of configured resource to display")
    @SpecialUse
    private String resourceName = "";

    @Option(name = "-a", aliases = {"--attributes"}, description = "Show resource attributes", required = false, multiValued = false)
    @SpecialUse
    private boolean showAttributes = false;

    @Option(name = "-e", aliases = {"--events"}, description = "Show resource events", required = false, multiValued = false)
    @SpecialUse
    private boolean showEvents = false;

    @SpecialUse
    @Option(name = "-o", aliases = {"--operations"}, description = "Show resource operations", required = false, multiValued = false)
    private boolean showOperations = false;

    private static void printParameters(final FeatureConfiguration feature, final StringBuilder output){
        for(final Map.Entry<String, String> param: feature.getParameters().entrySet())
            IOUtils.appendln(output, "%s=%s", param.getKey(), param.getValue());
    }

    private static void printAttribute(final String userDefinedName, final AttributeConfiguration attr, final StringBuilder output){
        IOUtils.appendln(output, userDefinedName);
        IOUtils.appendln(output, "Read/write Timeout: %s", attr.getReadWriteTimeout());
        printParameters(attr, output);
    }

    private static void printEvent(final String userDefinedName, final EventConfiguration ev, final StringBuilder output){
        IOUtils.appendln(output, userDefinedName);
        printParameters(ev, output);
    }

    private static void printOperation(final String userDefinedName, final OperationConfiguration op, final StringBuilder output){
        IOUtils.appendln(output, userDefinedName);
        IOUtils.appendln(output, "Invocation Timeout: %s", op.getInvocationTimeout());
        printParameters(op, output);
    }

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) throws InterruptedException {
        if (configuration.getManagedResources().containsKey(resourceName)) {
            final ManagedResourceConfiguration resource = configuration.getManagedResources().get(resourceName);
            IOUtils.appendln(output, "Resource Name: %s", resourceName);
            IOUtils.appendln(output, "Connection Type: %s", resource.getConnectionType());
            IOUtils.appendln(output, "Connection String: %s", resource.getConnectionString());
            IOUtils.appendln(output, "Configuration parameters:");
            for (final Map.Entry<String, String> pair : resource.getParameters().entrySet())
                IOUtils.appendln(output, "%s = %s", pair.getKey(), pair.getValue());
            checkInterrupted();
            if(showAttributes) {
                IOUtils.appendln(output, "==ATTRIBUTES==");
                for (final Map.Entry<String, ? extends AttributeConfiguration> attr : getFeatures(resource, AttributeConfiguration.class))
                    printAttribute(attr.getKey(), attr.getValue(), output);
                IOUtils.newLine(output);
            }
            checkInterrupted();
            if(showEvents){
                IOUtils.appendln(output, "==EVENTS==");
                for (final Map.Entry<String, ? extends EventConfiguration> attr : getFeatures(resource, EventConfiguration.class))
                    printEvent(attr.getKey(), attr.getValue(), output);
                IOUtils.newLine(output);
            }
            checkInterrupted();
            if(showOperations){
                IOUtils.appendln(output, "==OPERATIONS==");
                for (final Map.Entry<String, ? extends OperationConfiguration> attr : getFeatures(resource, OperationConfiguration.class))
                    printOperation(attr.getKey(), attr.getValue(), output);
                IOUtils.newLine(output);
            }
        } else
            output.append("Resource doesn't exist");
        return false;
    }
}
