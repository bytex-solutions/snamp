package com.itworks.snamp.adapters.ssh;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;

/**
 * Represents a command that prints a list of managed resources.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ListOfResourcesCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "resources";
    static final Options COMMAND_OPTIONS;

    static {
        COMMAND_OPTIONS = new Options();
    }

    static final String COMMAND_DESC = "Display list of connected managed resources";

    ListOfResourcesCommand(final AdapterController controller) {
        super(controller);
    }

    /**
     * Gets options associated with this command.
     *
     * @return The command options.
     */
    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    /**
     * Executes command synchronously.
     *
     * @param input  The command to execute.
     * @param output Output stream for the command execution result.
     */
    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) {
        for (final String managedResource : controller.getConnectedResources())
            output.println(managedResource);
    }
}
