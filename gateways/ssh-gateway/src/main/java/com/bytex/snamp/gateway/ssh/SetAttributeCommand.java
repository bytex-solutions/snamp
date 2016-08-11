package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.core.LogicalOperation;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.management.JMException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SetAttributeCommand extends AbstractAttributeCommand {
    static final String COMMAND_USAGE = String.format(SshAttributeAccessor.SET_COMMAND_PATTERN, "<name>", "<resource>", "<value-as-json>");
    static final String COMMAND_NAME = "set";
    static final String COMMAND_DESC = "Set attribute value";
    private static final Option VALUE_OPTION = new Option("v", "value", true, "Attribute value in JSON format");

    static final Options COMMAND_OPTIONS = new Options()
            .addOption(RESOURCE_OPTION)
            .addOption(NAME_OPTION)
            .addOption(VALUE_OPTION);

    SetAttributeCommand(final CommandExecutionContext context){
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }


    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        if(input.hasOption(RESOURCE_OPTION.getOpt()) && input.hasOption(NAME_OPTION.getOpt()) && input.hasOption(VALUE_OPTION.getOpt())){
            final String resourceName = input.getOptionValue(RESOURCE_OPTION.getOpt());
            final String attributeName = input.getOptionValue(NAME_OPTION.getOpt());
            final String attributeValue = input.getOptionValue(VALUE_OPTION.getOpt());
            if(!getGatewayController().processAttribute(resourceName, attributeName, attribute -> {
                try (final LogicalOperation ignored = SshHelpers.writeAttributeLogicalOperation(attribute.getOriginalName(), attributeName);
                     final StringReader reader = new StringReader(attributeValue)) {
                    attribute.setValue(reader);
                    output.println("OK");
                } catch (final JMException | IOException e) {
                    throw new CommandException(e);
                }
            })) throw new CommandException("Attribute %s doesn't exist.", attributeName);
        }
        else throw invalidCommandFormat();
    }
}
