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
final class HelpCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "help";
    private static final String COMMAND_DESC = "Display a set of available commands";
    static final Options COMMAND_OPTIONS = EMPTY_OPTIONS;

    private final HelpFormatter formatter;

    HelpCommand(final CommandExecutionContext context){
        super(context);
        formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("");
    }

    private static void printHelp(final HelpFormatter formatter,
                             final PrintWriter writer,
                             final String commandName,
                             final String commandDescr,
                             final Options opts) {
        formatter.printHelp(writer,
                HelpFormatter.DEFAULT_WIDTH,
                commandName,
                commandDescr,
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
     * @param input A command to apply.
     * @param output Output stream for the command execution result.
     */
    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) {
        printHelp(formatter, output, COMMAND_NAME, COMMAND_DESC, COMMAND_OPTIONS);
        printHelp(formatter, output, ListOfResourcesCommand.COMMAND_NAME, ListOfResourcesCommand.COMMAND_DESC, ListOfResourcesCommand.COMMAND_OPTIONS);
        printHelp(formatter, output, ListOfAttributesCommand.COMMAND_USAGE, ListOfAttributesCommand.COMMAND_DESC, ListOfAttributesCommand.COMMAND_OPTIONS);
        output.println();
        printHelp(formatter, output, GetAttributeCommand.COMMAND_USAGE, GetAttributeCommand.COMMAND_DESC, GetAttributeCommand.COMMAND_OPTIONS);
        printHelp(formatter, output, SetAttributeCommand.COMMAND_USAGE, SetAttributeCommand.COMMAND_DESC, SetAttributeCommand.COMMAND_OPTIONS);
        output.println();
        printHelp(formatter, output, NotificationsCommand.COMMAND_USAGE, NotificationsCommand.COMMAND_DESC, NotificationsCommand.COMMAND_OPTIONS);
        output.println();
        printHelp(formatter, output, ExitCommand.COMMAND_NAME, ExitCommand.COMMAND_DESC, COMMAND_OPTIONS);
    }

    static Completer createCommandCompleter(){
        return new StringsCompleter(COMMAND_NAME,
                ListOfResourcesCommand.COMMAND_NAME,
                ExitCommand.COMMAND_NAME,
                ListOfAttributesCommand.COMMAND_NAME,
                GetAttributeCommand.COMMAND_NAME,
                SetAttributeCommand.COMMAND_NAME,
                NotificationsCommand.COMMAND_NAME);
    }
}
