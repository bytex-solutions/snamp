package com.bytex.snamp.gateway.xmpp.client;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ChatMessageEvent extends CompletableFuture<Message> implements ChatMessageListener {
    private final Pattern filter;

    ChatMessageEvent(final String filter){
        this.filter = Pattern.compile(filter);
    }

    boolean processMessage(final Message message){
        if(message.getBody() == null) return false;
        else if(filter.matcher(message.getBody()).matches()) return false;
        else {
            complete(message);
            return true;
        }
    }

    @Override
    public void processMessage(final Chat chat, final Message message) {
        processMessage(message);
    }
}
