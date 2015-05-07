package com.itworks.snamp.adapters.xmpp.client;

import com.itworks.snamp.concurrent.SynchronizationEvent;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.regex.Pattern;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ChatMessageEvent extends SynchronizationEvent<Message> implements ChatMessageListener {
    private final Pattern filter;

    ChatMessageEvent(final String filter){
        super(false);
        this.filter = Pattern.compile(filter);
    }

    boolean processMessage(final Message message){
        if(message.getBody() == null) return false;
        else if(filter.matcher(message.getBody()).matches()) return false;
        else {
            fire(message);
            return true;
        }
    }

    @Override
    public void processMessage(final Chat chat, final Message message) {
        processMessage(message);
    }
}
