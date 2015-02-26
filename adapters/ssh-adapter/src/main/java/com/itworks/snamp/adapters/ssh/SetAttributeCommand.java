package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.WriteAttributeLogicalOperation;
import com.itworks.snamp.core.LogicalOperation;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.management.JMException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SetAttributeCommand extends AbstractManagementShellCommand {
    static final String COMMAND_USAGE = "set <attribute-id> <value-as-json>";
    static final String COMMAND_NAME = "set";
    static final String COMMAND_DESC = "Override value of the attribute";

    static final Options COMMAND_OPTIONS = EMPTY_OPTIONS;

    SetAttributeCommand(final CommandExecutionContext context){
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }


    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final String[] arguments = input.getArgs();
        switch (arguments.length){
            case 2:
                final String attributeID = arguments[0], value = arguments[1];
                if(!getAdapterController().processAttribute(attributeID, new Consumer<SshAttributeView, CommandException>() {
                    @Override
                    public void accept(final SshAttributeView attribute) throws CommandException {
                        try (final LogicalOperation ignored = new WriteAttributeLogicalOperation(attribute.getOriginalName(), attributeID);
                            final StringReader reader = new StringReader(value)) {
                            attribute.setValue(reader);
                            output.println("OK");
                        } catch (final JMException | IOException e) {
                            throw new CommandException(e);
                        }
                    }
                }))
                    throw new CommandException("Attribute %s doesn't exist.", attributeID);
            default: throw invalidCommandFormat();
        }
    }
}
