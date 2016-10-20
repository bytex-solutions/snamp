package com.bytex.snamp.gateway.xmpp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GetAttributeCommand extends AbstractAttributeCommand {
    static final String NAME = "get";
    static final String COMMAND_USAGE = String.format(XMPPAttributeAccessor.GET_COMMAND_PATTERN, "<name>", "<resource") + " [-t|-j]";
    static final String COMMAND_DESC = "Print attribute value";
    private static final Option TEXT_OPTION = new Option("t", "text", false, "Print attribute value in plain text format");
    private static final Option JSON_OPTION = new Option("j", "json", false, "Print attribute value in JSON format");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(RESOURCE_OPTION)
            .addOption(NAME_OPTION)
            .addOption(TEXT_OPTION)
            .addOption(JSON_OPTION);

    private final AttributeReader reader;

    GetAttributeCommand(final AttributeReader reader){
        super(COMMAND_OPTIONS);
        this.reader = Objects.requireNonNull(reader);
    }

    private Message doCommand(final String resourceName,
                                 final String attributeID,
                                 final AttributeValueFormat format) throws CommandException{
        final Collection<ExtensionElement> extensions =
                new LinkedList<>();
        final String value = callAndWrapException(() -> reader.getAttribute(resourceName, attributeID, format, extensions), CommandException::new);
        final Message result = new Message();
        result.setSubject(String.format("Value of '%s/%s'", resourceName, attributeID));
        result.setBody(value);
        result.addExtensions(extensions);
        return result;
    }

    @Override
    protected Message doCommand(final CommandLine input) throws CommandException {
        if(input.hasOption(RESOURCE_OPTION.getOpt()) && input.hasOption(NAME_OPTION.getOpt())) {
            final AttributeValueFormat format = input.hasOption(JSON_OPTION.getOpt()) ?
                    AttributeValueFormat.JSON :
                    AttributeValueFormat.TEXT;
            final String resourceName = input.getOptionValue(RESOURCE_OPTION.getOpt());
            final String attributeName = input.getOptionValue(NAME_OPTION.getOpt());
            return doCommand(resourceName, attributeName, format);
        }
        else throw new InvalidCommandFormatException();
    }
}
