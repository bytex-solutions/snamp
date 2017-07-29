package com.bytex.snamp.gateway.xmpp;

import org.apache.commons.cli.CommandLine;
import org.jivesoftware.smack.packet.Message;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class UnknownCommand extends AbstractCommand {
    private final String commandName;

    UnknownCommand(final String commandName){
        this.commandName = commandName;
    }

    @Override
    protected Message doCommand(final CommandLine command) {
        final Message message = new Message();
        message.setBody(String.format("Oops! Unsupported command %s", commandName));
        message.setType(Message.Type.error);
        return message;
    }
}
