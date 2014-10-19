package com.itworks.snamp.adapters.ssh;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

/**
 * Represents shell command that obtains a list of attributes.
 * This class canntot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ListOfAttributesCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "attr-list";
    static final Options COMMAND_OPTIONS = new Options();
    static final String COMMAND_DESC = "Display attributes of connected managed resources";

    private static final String RESOURCE_NAME_OPT = "r";
    private static final String SHOW_NAMES_OPT = "n";
    private static final String SHOW_DETAILS_OPT = "d";

    static{
        COMMAND_OPTIONS.addOption(new Option(RESOURCE_NAME_OPT, "resource", true, "The name of the managed resource used as a" +
                "filter for list of all attributes of connected managed resources"));
        COMMAND_OPTIONS.addOption(new Option(SHOW_NAMES_OPT, "names", false, "Display names with IDs of attributes"));
        COMMAND_OPTIONS.addOption(new Option(SHOW_DETAILS_OPT, "details", false, "Display details on attribute"));
    }

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
                                 final PrintWriter output) throws TimeoutException {
        for (final String attributeID : getAdapterController().getAttributes(resourceName)){
            final SshAttributeView attr = getAdapterController().getAttribute(attributeID);
            output.println(withNames ? String.format("ID: %s NAME: %s CAN_READ: %s CAN_WRITE %s", attributeID, attr.getName(), attr.canRead(), attr.canWrite()) : attributeID);
            if(details) {
                attr.printOptions(output);
                output.println();
            }
        }
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final boolean withNames = input.hasOption(SHOW_NAMES_OPT);
        final boolean details = input.hasOption(SHOW_DETAILS_OPT);
        final String resourceName = input.getOptionValue(RESOURCE_NAME_OPT, "");
        try {
            if (resourceName.isEmpty())
                for (final String r : getAdapterController().getConnectedResources())
                    printAttributes(r, withNames, details, output);
            else printAttributes(resourceName, withNames, details, output);
        }
        catch (final TimeoutException e){
            throw new CommandException(e);
        }
    }
}
