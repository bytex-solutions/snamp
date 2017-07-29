package com.bytex.snamp.gateway.xmpp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.Message;

import java.util.Objects;

/**
 * Prints list of attributes.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class ListOfAttributesCommand extends AbstractCommand {
    static final String NAME = "attr-list";
    static final String COMMAND_USAGE = "attr-list [-s] [-d] [-r resource-name]";
    private static final Option SHOW_NAMES_OPTION = new Option("s", "names", false, "Display resource-dependent names of attributes");
    private static final Option SHOW_DETAILS_OPTION = new Option("d", "details", false, "Display details on attribute");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(RESOURCE_OPTION)
            .addOption(SHOW_NAMES_OPTION)
            .addOption(SHOW_DETAILS_OPTION);
    static final String COMMAND_DESC = "Display attributes of connected managed resources";

    private final AttributeReader reader;

    ListOfAttributesCommand(final AttributeReader reader) {
        super(COMMAND_OPTIONS);
        this.reader = Objects.requireNonNull(reader);
    }

    private void printAttributes(final String resourceName,
                                 final boolean withNames,
                                 final boolean details,
                                 final StringBuilder output) {
        for (final String attributeName : reader.getResourceAttributes(resourceName))
            output.append(reader.printOptions(resourceName, attributeName, withNames, details)).append(System.lineSeparator());
    }

    @Override
    protected Message doCommand(final CommandLine input) {
        final boolean withNames = input.hasOption(SHOW_NAMES_OPTION.getOpt());
        final boolean details = input.hasOption(SHOW_DETAILS_OPTION.getOpt());
        final String resourceName = input.getOptionValue(RESOURCE_OPTION.getOpt(), "");
        final Message msg = new Message();
        {
            final StringBuilder output = new StringBuilder(64);
            if (resourceName.isEmpty())
                for (final String r : reader.getHostedResources())
                    printAttributes(r, withNames, details, output);
            else printAttributes(resourceName, withNames, details, output);
            msg.setSubject("List of attributes");
            msg.setBody(output.toString());
        }
        return msg;
    }
}
