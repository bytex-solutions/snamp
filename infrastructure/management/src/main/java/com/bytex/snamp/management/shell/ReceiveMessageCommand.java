package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.Communicator;
import org.apache.karaf.shell.commands.Command;

import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.management.shell.Utils.appendln;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "receive-message",
        description = "Receive message posted by another cluster member")
public final class ReceiveMessageCommand extends MessageCommand {

    @Override
    protected CharSequence doExecute() throws InterruptedException, TimeoutException {
        final Communicator communicator = getCommunicator();
        session.getConsole().format("Waiting input message. Press Ctrl+C to abort");
        final Communicator.IncomingMessage message = communicator.receiveMessage(Communicator.ANY_MESSAGE, null);
        if(message == null)
            return "No message received";
        final StringBuilder result = new StringBuilder();
        appendln(result, "From: %s<%s>", message.getSender().getName(), message.getSender().getAddress());
        appendln(result, "Message ID: %s", message.getMessageID());
        appendln(result, "Message: %s", message.getPayload());
        return result;
    }
}
