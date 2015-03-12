package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.ReadAttributeLogicalOperation;
import com.itworks.snamp.core.LogicalOperation;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.management.JMException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GetAttributeCommand extends AbstractManagementShellCommand {
    static final String COMMAND_USAGE = "get <attribute-id> [text|json]";
    static final String COMMAND_NAME = "get";
    static final Options COMMAND_OPTIONS = EMPTY_OPTIONS;
    static final String COMMAND_DESC = "Display attribute value";

    GetAttributeCommand(final CommandExecutionContext context){
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        //each argument is an attribute identifier
        String[] arguments = input.getArgs();
        switch (arguments.length){
            case 1: arguments = new String[]{arguments[0], "text"};
            case 2:
                final String attributeID = arguments[0], format = arguments[1];
                if(getAdapterController().processAttribute(attributeID, new Consumer<SshAttributeView, CommandException>() {
                    @Override
                    public void accept(final SshAttributeView attribute) throws CommandException {
                        try(final LogicalOperation ignored = new ReadAttributeLogicalOperation(attribute.getOriginalName(), attributeID)) {
                            switch (format){
                                case "json": attribute.printValue(output, AttributeValueFormat.JSON); return;
                                default: attribute.printValue(output, AttributeValueFormat.TEXT);
                            }
                        }
                        catch (final IOException | JMException e){
                            throw new CommandException(e);
                        }
                    }
                })) return;
                else throw new CommandException("Attribute %s doesn't exist", attributeID);
            default:
                throw invalidCommandFormat();
        }
    }
}
