package com.itworks.snamp.adapters.ssh;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GetAttributeCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "get";
    static final Options COMMAND_OPTIONS = new Options();

    public GetAttributeCommand(final AdapterController controller){
        super(controller);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        //each argument is an attribute identifier
        for(final String attributeID: input.getArgs()){
            final SshAttributeView attr = controller.getAttribute(attributeID);
            if(attr == null) throw new CommandException("Attribute %s doesn't exist", attributeID);
            try {
                attr.printValue(output);
            }
            catch (final TimeoutException e) {
                throw new CommandException(e);
            }
        }
    }
}
