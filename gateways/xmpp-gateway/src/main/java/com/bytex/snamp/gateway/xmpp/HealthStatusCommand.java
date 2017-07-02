package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.MalfunctionStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.Message;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Prints health status of the group or resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HealthStatusCommand extends AbstractCommand {
    static final String NAME = "hs";
    static final String COMMAND_USAGE = "hs [-r <resource-name>] | [-g <group-name>]";
    private static final Option GROUP_NAME_OPTION = new Option("g", "group", true, "Use group name instead of resource name");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(RESOURCE_OPTION)
            .addOption(GROUP_NAME_OPTION);
    static final String COMMAND_DESC = "Display health status of the group or resource";

    HealthStatusCommand(){
        super(COMMAND_OPTIONS);
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

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    private void printResourceStatus(final String resourceName, final PrintWriter output) throws CommandException {
        final HealthStatus status = ManagedResourceConnectorClient.getStatus(getBundleContext(), resourceName).orElse(null);
        if (status == null)
            throw new CommandException("Resource %s doesn't exist", resourceName);
        else
            printHealthStatus(status, output);
    }

    private void printGroupStatus(final String groupName, final PrintWriter output) throws CommandException {
        final ResourceGroupHealthStatus status = SupervisorClient.getGroupStatus(getBundleContext(), groupName);
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
    protected Message doCommand(final CommandLine input) throws CommandException {
        final String resourceName = input.getOptionValue(RESOURCE_OPTION.getOpt(), "");
        final String groupName = input.getOptionValue(GROUP_NAME_OPTION.getOpt(), "");
        final String messageText;
        try (final StringWriter writer = new StringWriter(); final PrintWriter printer = new PrintWriter(writer, true)) {
            if (!resourceName.isEmpty())
                printResourceStatus(resourceName, printer);
            else if (!groupName.isEmpty())
                printGroupStatus(groupName, printer);
            else
                throw new InvalidCommandFormatException();
            messageText = writer.toString();
        } catch (final IOException e) {
            throw new CommandException(e);
        }
        final Message result = new Message();
        result.setSubject("Health status");
        result.setBody(messageText);
        return result;
    }
}
