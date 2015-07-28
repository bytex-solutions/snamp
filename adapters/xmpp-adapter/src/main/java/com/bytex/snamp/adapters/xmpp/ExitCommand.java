package com.bytex.snamp.adapters.xmpp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ExitCommand extends AbstractCommand {
    static final Options COMMAND_OPTIONS = new Options();
    private final Chat chatSession;
    static final String NAME = "exit";
    static final String COMMAND_DESC = "Close the current terminal session";

    ExitCommand(final Chat chat){
        super(COMMAND_OPTIONS);
        this.chatSession = chat;
    }

    @Override
    protected Message doCommand(final CommandLine command) throws CommandException {
        try {
            chatSession.sendMessage("Bye!");
        } catch (SmackException.NotConnectedException e) {
            throw new CommandException(e);
        }
        finally {
            chatSession.close();
        }
        return null;
    }
}
