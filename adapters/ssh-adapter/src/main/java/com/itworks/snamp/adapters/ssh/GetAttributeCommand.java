package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.ReadAttributeLogicalOperation;
import com.itworks.snamp.core.LogicalOperation;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.management.JMException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GetAttributeCommand extends AbstractAttributeCommand {
    static final String COMMAND_USAGE = "get -n <name> -r <resource> [-t|-j]";
    static final String COMMAND_NAME = "get";
    static final String COMMAND_DESC = "Print attribute value";
    private static final Option TEXT_OPTION = new Option("t", "text", false, "Print attribute value in plain text format");
    private static final Option JSON_OPTION = new Option("j", "json", false, "Print attribute value in JSON format");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(RESOURCE_OPTION)
            .addOption(NAME_OPTION)
            .addOption(TEXT_OPTION)
            .addOption(JSON_OPTION);

    GetAttributeCommand(final CommandExecutionContext context){
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        if(input.hasOption(RESOURCE_OPTION.getOpt()) && input.hasOption(NAME_OPTION.getOpt())){
            final AttributeValueFormat format = input.hasOption(JSON_OPTION.getOpt()) ?
                    AttributeValueFormat.JSON:
                    AttributeValueFormat.TEXT;
            final String resourceName = input.getOptionValue(RESOURCE_OPTION.getOpt());
            final String attributeName = input.getOptionValue(NAME_OPTION.getOpt());
            if(!getAdapterController().processAttribute(resourceName, attributeName, new Consumer<SshAttributeMapping, CommandException>() {
                @Override
                public void accept(final SshAttributeMapping attribute) throws CommandException {
                    try(final LogicalOperation ignored = new ReadAttributeLogicalOperation(attribute.getOriginalName(), attributeName)) {
                        attribute.printValue(output, format);
                    }
                    catch (final IOException | JMException e){
                        throw new CommandException(e);
                    }
                }
            })) throw new CommandException("Attribute %s doesn't exist", attributeName);
        }
        else throw invalidCommandFormat();
    }
}
