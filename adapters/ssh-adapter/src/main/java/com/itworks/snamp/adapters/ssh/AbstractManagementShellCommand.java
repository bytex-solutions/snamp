package com.itworks.snamp.adapters.ssh;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.sshd.server.Command;

import java.io.PrintWriter;
import java.util.Objects;

/**
 * Represents SSH adapter shell command.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractManagementShellCommand extends BasicParser implements ManagementShellCommand {
    static Options EMPTY_OPTIONS = new Options();

    static class CommandException extends Exception{
        public CommandException(final String message, final Object... args){
            super(String.format(message, args));
        }
        public CommandException(final Throwable cause){
            super(cause);
        }
    }

    protected final AdapterController controller;

    protected AbstractManagementShellCommand(final AdapterController controller){
        this.controller = Objects.requireNonNull(controller, "controller is null.");
    }

    protected abstract Options getCommandOptions();

    protected abstract void doCommand(final CommandLine input, final PrintWriter output) throws CommandException;

    @Override
    public final void doCommand(final String[] arguments,
                          final PrintWriter outStream,
                          final PrintWriter errStream) {
        try {
            final CommandLine input = parse(getCommandOptions(), arguments);
            doCommand(input, outStream);
        } catch (final ParseException | CommandException e) {
            errStream.println(e.getMessage());
        }
    }

    @Override
    public final Command createSshCommand(final String[] arguments) {
        return null;
    }
}
