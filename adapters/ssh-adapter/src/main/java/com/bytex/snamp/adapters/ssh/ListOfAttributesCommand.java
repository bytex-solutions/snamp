package com.bytex.snamp.adapters.ssh;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Represents shell command that obtains a list of attributes.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ListOfAttributesCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "attr-list";
    static final String COMMAND_USAGE = "attr-list [-s] [-d] [-r <resource-name>]";
    private static final Option SHOW_NAMES_OPTION = new Option("s", "names", false, "Display resource-dependent names of attributes");
    private static final Option SHOW_DETAILS_OPTION = new Option("d", "details", false, "Display details on attribute");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(RESOURCE_OPTION)
            .addOption(SHOW_NAMES_OPTION)
            .addOption(SHOW_DETAILS_OPTION);
    static final String COMMAND_DESC = "Display attributes of connected managed resources";


    ListOfAttributesCommand(final CommandExecutionContext context) {
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    private void printAttributes(final String resourceName,
                                 final boolean withNames,
                                 final boolean details,
                                 final PrintWriter output) throws IOException {
        for (final String attributeName : getAdapterController().getAttributes(resourceName))
            getAdapterController().processAttribute(resourceName, attributeName, attr -> {
                output.println(withNames ? String.format("ID: %s NAME: %s CAN_READ: %s CAN_WRITE %s", attributeName, attr.getOriginalName(), attr.canRead(), attr.canWrite()) : attributeName);
                if(details) {
                    attr.printOptions(output);
                    output.println();
                }
            });
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final boolean withNames = input.hasOption(SHOW_NAMES_OPTION.getOpt());
        final boolean details = input.hasOption(SHOW_DETAILS_OPTION.getOpt());
        final String resourceName = input.getOptionValue(RESOURCE_OPTION.getOpt(), "");
        try {
            if (resourceName.isEmpty())
                for (final String r : getAdapterController().getConnectedResources())
                    printAttributes(r, withNames, details, output);
            else printAttributes(resourceName, withNames, details, output);
        }
        catch (final IOException e){
            throw new CommandException(e);
        }
    }
}
