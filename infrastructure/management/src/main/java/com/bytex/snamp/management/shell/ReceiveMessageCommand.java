package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.Communicator;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "receive-message",
        description = "Receive message posted by another cluster member")
@Service
public final class ReceiveMessageCommand extends MessageCommand {
    @Reference
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private Session session;

    @Override
    public void execute(final PrintWriter output) throws InterruptedException, TimeoutException {
        final Communicator communicator = getCommunicator();
        session.getConsole().format("Waiting input message. Press Ctrl+C to abort");
        final Communicator.IncomingMessage message = communicator.receiveMessage(Communicator.ANY_MESSAGE, Function.identity(), null);
        if(message == null)
            output.println("No message received");
        else {
            final StringBuilder result = new StringBuilder();
            output.format("From: %s<%s>", message.getSender().getName(), message.getSender().getAddress()).println();
            output.format("Message ID: %s", message.getMessageID()).println();
            output.format("Message: %s", message.getPayload()).println();
        }
    }
}
