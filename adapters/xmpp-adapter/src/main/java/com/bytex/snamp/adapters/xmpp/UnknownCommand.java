package com.bytex.snamp.adapters.xmpp;

import org.apache.commons.cli.CommandLine;
import org.jivesoftware.smack.packet.Message;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class UnknownCommand extends AbstractCommand {
    private final String commandName;

    UnknownCommand(final String commandName){
        this.commandName = commandName;
    }

    @Override
    protected Message doCommand(final CommandLine command) {
        return new Message(String.format("Oops! Unsupported command %s", commandName), Message.Type.error);
    }
}
