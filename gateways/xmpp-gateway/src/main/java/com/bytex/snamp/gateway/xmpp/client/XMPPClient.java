package com.bytex.snamp.gateway.xmpp.client;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;
import com.bytex.snamp.gateway.xmpp.XMPPGatewayConfigurationProvider;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
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

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Represents simple XMPP client that can be used in integration tests.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class XMPPClient implements Closeable {

    private final AbstractXMPPConnection connection;
    private final String domain;
    private Chat chat = null;

    public XMPPClient(final Map<String, String> parameters) throws AbsentConfigurationParameterException, GeneralSecurityException, IOException {
        connection = XMPPGatewayConfigurationProvider.createConnection(parameters);
        domain = XMPPGatewayConfigurationProvider.getDomain(parameters);
    }

    public boolean beginChat(final String userName) {
        if(chat == null) {
            final ChatManager manager = ChatManager.getInstanceFor(connection);
            chat = manager.createChat(userName + '@' + domain);
            return true;
        }
        else return false;
    }

    public void peekMessage(final String message) throws IOException {
        if (chat == null)
            throw new IOException("Chat doesn't exist");
        else
            callAndWrapException(() -> {
                chat.sendMessage(message);
                return null;
            }, IOException::new);
    }

    public String sendMessage(final String message,
                              final String ignoreFilter,
                              final Duration responseTimeout) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        if (chat == null) throw new IOException("Chat doesn't exist");
        final ChatMessageEvent response = new ChatMessageEvent(ignoreFilter);
        chat.addMessageListener(response);
        callAndWrapException(() -> {
            chat.sendMessage(message);
            return null;
        }, IOException::new);
        final Message responseMsg;
        try {
            responseMsg = response.get(responseTimeout.toNanos(), TimeUnit.NANOSECONDS);
        } finally {
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
        callAndWrapException(() -> {
            connection.connect();
            connection.login();
            return null;
        }, IOException::new);
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
