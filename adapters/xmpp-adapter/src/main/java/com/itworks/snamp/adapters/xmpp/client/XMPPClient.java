package com.itworks.snamp.adapters.xmpp.client;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.xmpp.XMPPAdapterConfiguration;
import com.itworks.snamp.configuration.AbsentConfigurationParameterException;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.io.Closeable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Represents simple XMPP client that can be used in integration tests.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XMPPClient implements Closeable {

    private final AbstractXMPPConnection connection;
    private final String domain;
    private Chat chat = null;

    public XMPPClient(final Map<String, String> parameters) throws AbsentConfigurationParameterException, GeneralSecurityException, IOException {
        connection = XMPPAdapterConfiguration.createConnection(parameters);
        domain = XMPPAdapterConfiguration.getDomain(parameters);
    }

    public boolean beginChat(final String userName){
        if(chat == null) {
            final ChatManager manager = ChatManager.getInstanceFor(connection);
            chat = manager.createChat(userName + '@' + domain);
            return true;
        }
        else return false;
    }

    public String sendMessageSync(final String message,
                                  final String ignoreFilter,
                                  final TimeSpan responseTimeout) throws IOException, TimeoutException, InterruptedException {
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
            responseMsg = response.getAwaitor().await(responseTimeout);
        }
        finally {
            chat.removeMessageListener(response);
        }
        return responseMsg.getBody();
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
        }
    }

    @Override
    public String toString() {
        return connection.toString();
    }
}
