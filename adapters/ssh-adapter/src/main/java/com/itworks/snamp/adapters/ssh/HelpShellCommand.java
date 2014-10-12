package com.itworks.snamp.adapters.ssh;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;

/**
 * Represents documentation printer.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HelpShellCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "help";
    static final Options COMMAND_OPTIONS = new Options();

    private final HelpFormatter formatter;

    HelpShellCommand(final AdapterController controller){
        super(controller);
        formatter = new HelpFormatter();
    }

    private static void printHelp(final HelpFormatter formatter,
                             final PrintWriter writer,
                             final String commandName,
                             final Options opts) {
        formatter.printHelp(writer,
                HelpFormatter.DEFAULT_WIDTH,
                commandName,
                null,
                opts,
                HelpFormatter.DEFAULT_LEFT_PAD,
                HelpFormatter.DEFAULT_DESC_PAD,
                null,
                false);
    }

    /**
     * Gets options associated with this command.
     *
     * @return The command options.
     */
    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    /**
     * Executes command synchronously.
     * @param input A command to execute.
     * @param output Output stream for the command execution result.
     */
    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) {
            printHelp(formatter, output, COMMAND_NAME, COMMAND_OPTIONS);
            printHelp(formatter, output, ListOfResourcesCommand.COMMAND_NAME, ListOfResourcesCommand.COMMAND_OPTIONS);
    }

    static Completer createCommandCompleter(){
        return new StringsCompleter(COMMAND_NAME, ListOfResourcesCommand.COMMAND_NAME, ExitCommand.COMMAND_NAME);
    }
}
