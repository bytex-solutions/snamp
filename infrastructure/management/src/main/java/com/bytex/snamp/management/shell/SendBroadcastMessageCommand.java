package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.Communicator;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "post-message",
        description = "Send text message to all members in cluster")
public final class SendBroadcastMessageCommand extends MessageCommand {
    @Argument(index = 0, required = true, name = "message", description = "A message to send")
    @SpecialUse
    private String message = "";

    @Override
    protected String doExecute() throws Exception {
        final Communicator communicator = getCommunicator();
        communicator.sendSignal(message);
        return String.format("Message posted successfully: '%s'", message);
    }
}
