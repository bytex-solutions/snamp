package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.Communicator;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
        name = "post-message",
        description = "Send text message to all members in cluster")
@Service
public final class SendBroadcastMessageCommand extends MessageCommand {
    @Argument(index = 0, required = true, name = "message", description = "A message to send")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String message = "";

    @Override
    public void execute(final PrintWriter output) throws Exception {
        final Communicator communicator = getCommunicator();
        communicator.sendSignal(message);
        output.format("Message posted successfully: '%s'", message);
    }
}
