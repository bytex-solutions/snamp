package com.bytex.snamp.adapters.ssh;

import org.apache.sshd.server.Command;

import java.io.PrintWriter;

/**
 * Represents management shell command.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
interface ManagementShellCommand {
    void doCommand(final String[] arguments,
                                final PrintWriter outStream,
                                final PrintWriter errStream);

    Command createSshCommand(final String[] arguments);
}
