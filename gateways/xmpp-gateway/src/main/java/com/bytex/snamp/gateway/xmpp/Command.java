package com.bytex.snamp.gateway.xmpp;

import org.jivesoftware.smack.packet.Message;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
interface Command {
    Message doCommand(final String[] arguments) throws CommandException;
}
