package com.itworks.snamp.adapters.ssh;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintStream;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Represents SSH adapter shell command.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractManagementShellCommand extends BasicParser {
    static Options EMPTY_OPTIONS = new Options();

    static class CommandException extends Exception{
        public CommandException(final String message, final Object... args){
            super(String.format(message, args));
        }
    }

    private final AdapterController controller;
    private final Logger logger;

    protected AbstractManagementShellCommand(final AdapterController controller,
                                             final Logger logger){
        this.controller = Objects.requireNonNull(controller, "controller is null.");
        this.logger = Objects.requireNonNull(logger, "logger is null.");
    }

    protected abstract Options getCommandOptions();

    protected abstract void doCommand(final CommandLine input, final PrintStream output) throws CommandException;

    public final void doCommand(final String[] arguments,
                          final PrintStream outStream,
                          final PrintStream errStream) {
        try {
            final CommandLine input = parse(getCommandOptions(), arguments);
            doCommand(input, outStream);
        }
        catch (final ParseException | CommandException e){
            errStream.println(e.getMessage());
        }
    }
}
