package com.bytex.snamp.adapters.xmpp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class HelpCommand extends AbstractCommand {
    static final String NAME = "help";
    private static final String COMMAND_DESC = "Display a set of available commands";

    private final HelpFormatter formatter;

    HelpCommand(){
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

    @Override
    protected Message doCommand(final CommandLine command) throws CommandException {
        final Message result = new Message();
        result.setSubject("Help");
        try(final StringWriter output = new StringWriter(1024);
            final PrintWriter writer = new PrintWriter(output)){
            printHelp(formatter, writer, NAME, COMMAND_DESC, getOptions());
            writer.println();
            printHelp(formatter, writer, GetAttributeCommand.COMMAND_USAGE, GetAttributeCommand.COMMAND_DESC, GetAttributeCommand.COMMAND_OPTIONS);
            printHelp(formatter, writer, SetAttributeCommand.COMMAND_USAGE, SetAttributeCommand.COMMAND_DESC, SetAttributeCommand.COMMAND_OPTIONS);
            writer.println();
            printHelp(formatter, writer, ListOfAttributesCommand.COMMAND_USAGE, ListOfAttributesCommand.COMMAND_DESC, ListOfResourcesCommand.COMMAND_OPTIONS);
            printHelp(formatter, writer, ListOfResourcesCommand.NAME, ListOfResourcesCommand.COMMAND_DESC, ListOfResourcesCommand.COMMAND_OPTIONS);
            printHelp(formatter, writer, ManageNotificationsCommand.COMMAND_USAGE, ManageNotificationsCommand.COMMAND_DESC, ManageNotificationsCommand.COMMAND_OPTIONS);
            printHelp(formatter, writer, ExitCommand.NAME, ExitCommand.COMMAND_DESC, ExitCommand.COMMAND_OPTIONS);
            result.setBody(output.toString());
        } catch (final IOException e) {
            throw new CommandException(e);
        }
        return result;
    }
}
