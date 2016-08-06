package com.bytex.snamp.adapters.xmpp.client;

import com.bytex.snamp.adapters.xmpp.XMPPAdapterConfigurationProvider;
import com.bytex.snamp.configuration.AbsentConfigurationParameterException;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.io.Closeable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents simple XMPP client that can be used in integration tests.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class XMPPClient implements Closeable {

    private final AbstractXMPPConnection connection;
    private final String domain;
    private Chat chat = null;

    public XMPPClient(final Map<String, String> parameters) throws AbsentConfigurationParameterException, GeneralSecurityException, IOException {
        connection = XMPPAdapterConfigurationProvider.createConnection(parameters);
        domain = XMPPAdapterConfigurationProvider.getDomain(parameters);
    }

    public boolean beginChat(final String userName){
        if(chat == null) {
            final ChatManager manager = ChatManager.getInstanceFor(connection);
            chat = manager.createChat(userName + '@' + domain);
            return true;
        }
        else return false;
    }

    public void peekMessage(final String message) throws IOException {
        if(chat == null) throw new IOException("Chat doesn't exist");
        else try {
            chat.sendMessage(message);
        } catch (final SmackException.NotConnectedException e) {
            throw new IOException(e);
        }
    }

    public String sendMessage(final String message,
                              final String ignoreFilter,
                              final Duration responseTimeout) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        if(chat == null) throw new IOException("Chat doesn't exist");
        final ChatMessageEvent response = new ChatMessageEvent(ignoreFilter);
        chat.addMessageListener(response);
        try {
            chat.sendMessage(message);
        } catch (final SmackException.NotConnectedException e) {
            throw new IOException(e);
        }
        final Message responseMsg;
        try{
            responseMsg = response.get(responseTimeout.toNanos(), TimeUnit.NANOSECONDS);
        }
        finally {
            chat.removeMessageListener(response);
        }
        return responseMsg.getBody();
    }

    private ChatMessageListener createMessageListener(final ChatMessageEvent response){
        return new ChatMessageListener() {
            @Override
            public void processMessage(final Chat chat, final Message message) {
                if (response.processMessage(message))
                    chat.removeMessageListener(this);
            }
        };
    }

    public Future<Message> waitMessage(final String ignoreFilter) throws IOException {
        if (chat == null) throw new IOException("Chat doesn't exist");
        final ChatMessageEvent response = new ChatMessageEvent(ignoreFilter);
        chat.addMessageListener(createMessageListener(response));
        return response;
    }

    public boolean endChat(){
        if(chat == null) return false;
        chat.close();
        return true;
    }

    public void connectAndLogin() throws IOException {
        try {
            connection.connect();
            connection.login();
        } catch (final SmackException | XMPPException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (connection.isConnected()) {
            if(chat != null) chat.close();
            try {
                connection.disconnect(new Presence(Presence.Type.unavailable));
            } catch (final SmackException.NotConnectedException e) {
                throw new IOException(e);
            }
            finally {
                chat = null;
            }
        }
    }

    @Override
    public String toString() {
        return connection.toString();
    }

}
