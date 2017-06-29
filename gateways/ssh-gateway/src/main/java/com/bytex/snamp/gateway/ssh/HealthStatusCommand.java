package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.MalfunctionStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;

/**
 * Prints health status of the group or resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HealthStatusCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "hs";
    static final String COMMAND_USAGE = "hs [-r <resource-name>] | [-g <group-name>]";
    private static final Option GROUP_NAME_OPTION = new Option("g", "group", true, "Use group name instead of resource name");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(RESOURCE_OPTION)
            .addOption(GROUP_NAME_OPTION);
    static final String COMMAND_DESC = "Display health status of the group or resource";

    HealthStatusCommand(final CommandExecutionContext context) {
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    private static void printMalfunctionStatus(final MalfunctionStatus status, final PrintWriter writer){
        writer.format("Type: %s", status.getClass().getSimpleName()).println();
        writer.format("Malfunction level: %s", status.getLevel()).println();
        writer.format("Time stamp: %s", status.getTimeStamp()).println();
        writer.format("Data: %s", status.getData()).println();
    }

    private static void printHealthStatus(final HealthStatus status, final PrintWriter writer){
        if(status instanceof OkStatus)
            writer.println("Everything is fine");
        else if(status instanceof MalfunctionStatus)
            printMalfunctionStatus((MalfunctionStatus) status, writer);
        else
            writer.format("Unknown status: %s", status).println();
    }

    private void printResourceStatus(final String resourceName, final PrintWriter output) throws CommandException {
        final HealthStatus status = getGatewayController().getResourceStatus(resourceName);
        if (status == null)
            throw new CommandException("Resource %s doesn't exist", resourceName);
        else
            printHealthStatus(status, output);
    }

    private void printGroupStatus(final String groupName, final PrintWriter output) throws CommandException {
        final ResourceGroupHealthStatus status = getGatewayController().getGroupStatus(groupName);
        if(status == null)
            throw new CommandException("Group %s doesn't exist", groupName);
        else {
            output.println("==SUMMARY==");
            printHealthStatus(status.getSummaryStatus(), output);
            output.println();
            status.forEach((resourceName, resourceStatus) -> {
                output.format("STATUS OF %s", resourceName).println();
                printHealthStatus(resourceStatus, output);
                output.println();
            });
        }
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final String resourceName = input.getOptionValue(RESOURCE_OPTION.getOpt(), "");
        final String groupName = input.getOptionValue(GROUP_NAME_OPTION.getOpt(), "");
        if (!resourceName.isEmpty())
            printResourceStatus(resourceName, output);
        else if (!groupName.isEmpty())
            printGroupStatus(groupName, output);
        else
            throw invalidCommandFormat();
    }
}
