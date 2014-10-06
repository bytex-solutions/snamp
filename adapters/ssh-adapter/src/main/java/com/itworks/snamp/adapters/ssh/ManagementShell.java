package com.itworks.snamp.adapters.ssh;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.command.UnknownCommand;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ManagementShell implements CommandFactory {
    private static final String COMMAND_DELIMITIER = "\\p{javaWhitespace}+";

    private final AdapterController controller;

    public ManagementShell(final AdapterController controller) {
        this.controller = Objects.requireNonNull(controller, "controller is null.");
    }

    /**
     * Create a command with the given name.
     * If the command is not known, a dummy command should be returned to allow
     * the display output to be sent back to the client.
     *
     * @param command The command to execute.
     * @return a non null <code>Command</code>
     */
    @Override
    public Command createCommand(final String command) {
        return command != null && !command.isEmpty() ? createCommand(command.split(COMMAND_DELIMITIER)) : null;
    }

    private Command createCommand(final String command, final String[] args) {
        switch (command) {
            case HelpCommand.COMMAND_NAME:
                return new HelpCommand(controller);
            case ListOfResourcesCommand.COMMAND_NAME:
                return new ListOfResourcesCommand(controller);
            default:
                return new UnknownCommand(command);
        }
    }

    private Command createCommand(final String[] command) {
        return command.length > 0 ? createCommand(command[0], ArrayUtils.remove(command, 0)) : null;
    }
}
