package com.bytex.snamp.adapters.xmpp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.Message;

import javax.management.JMException;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class SetAttributeCommand extends AbstractAttributeCommand {
    static final String COMMAND_USAGE = String.format(XMPPAttributeAccessor.SET_COMMAND_PATTERN, "<name>", "<resource>", "<value-as-json>") + " [-s]";
    static final String NAME = "set";
    static final String COMMAND_DESC = "Set attribute value";
    private static final Option VALUE_OPTION = new Option("v", "value", true, "Attribute value in JSON format");
    private static final Option SILENT_OPTION = new Option("s", "silent", false, "Suppress response message");

    static final Options COMMAND_OPTIONS = new Options()
            .addOption(RESOURCE_OPTION)
            .addOption(NAME_OPTION)
            .addOption(SILENT_OPTION)
            .addOption(VALUE_OPTION);

    private final AttributeWriter writer;

    SetAttributeCommand(final AttributeWriter writer){
        super(COMMAND_OPTIONS);
        this.writer = writer;
    }

    private Message doCommand(final String resourceName,
                              final String attributeID,
                              final String attributeValue,
                              final boolean silent) throws CommandException{
        try {
            writer.setAttribute(resourceName, attributeID, attributeValue);
        } catch (final JMException e) {
            throw new CommandException(e);
        }
        if(silent) return null;
        else {
            final Message result = new Message();
            result.setSubject(String.format("Set '%s/%s' attribute", resourceName, attributeID));
            result.setBody("New value is ".concat(attributeValue));
            return result;
        }
    }

    @Override
    protected Message doCommand(final CommandLine input) throws CommandException {
        if(input.hasOption(RESOURCE_OPTION.getOpt()) && input.hasOption(NAME_OPTION.getOpt()) && input.hasOption(VALUE_OPTION.getOpt())){
            final String resourceName = input.getOptionValue(RESOURCE_OPTION.getOpt());
            final String attributeName = input.getOptionValue(NAME_OPTION.getOpt());
            final String attributeValue = input.getOptionValue(VALUE_OPTION.getOpt());
            return doCommand(resourceName,
                    attributeName,
                    attributeValue,
                    input.hasOption(SILENT_OPTION.getOpt()));
        }
        else throw new InvalidCommandFormatException();
    }
}
