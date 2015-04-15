package com.itworks.snamp.adapters.xmpp;

import org.jivesoftware.smack.packet.Message;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface Command {
    Message doCommand(final String[] arguments) throws CommandException;
}
