package com.bytex.snamp.adapters.ssh;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.command.UnknownCommand;

import java.io.PrintWriter;

/**
 * Represents unknown shell command.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class UnknownShellCommand implements ManagementShellCommand {
    private final String command;

    UnknownShellCommand(final String unknownCommand) {
        this.command = unknownCommand;
    }


    @Override
    public void doCommand(final String[] arguments, final PrintWriter outStream, final PrintWriter errStream) {
        outStream.println(String.format("Unknown command: %s", command));
    }

    @Override
    public Command createSshCommand(final String[] arguments) {
        return new UnknownCommand(command);
    }
}
