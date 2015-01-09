package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.internal.annotations.MethodStub;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ExitCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "exit";
    static final String COMMAND_DESC = "Close the current terminal session";

    ExitCommand(final CommandExecutionContext context) {
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return EMPTY_OPTIONS;
    }

    @Override
    @MethodStub
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {

    }
}
