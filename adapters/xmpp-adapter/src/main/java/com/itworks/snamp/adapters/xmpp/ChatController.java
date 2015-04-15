package com.itworks.snamp.adapters.xmpp;

import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.Utils;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.XMPPError;
import org.osgi.framework.BundleContext;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages chat sessions.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ChatController extends ThreadSafeObject implements ChatManagerListener, Closeable {
    private static final Pattern COMMAND_DELIMITER = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

    private static final class ChatSession implements ChatMessageListener{
        private final Logger logger;
        private final XMPPAttributeModel attributes;

        private ChatSession(final Chat chat,
                            final XMPPAttributeModel attributes,
                            final Logger logger){
            chat.addMessageListener(this);
            this.logger = logger;
            this.attributes = attributes;
        }

        private BundleContext getBundleContext(){
            return Utils.getBundleContextByObject(this);
        }

        @Override
        public void processMessage(final Chat chat, Message message) {
            if(message.getBody() == null) return;
            final String[] arguments = splitArguments(message.getBody());
            if (arguments.length == 0) {
                try {
                    message = new Message();
                    message.setSubject("Empty request");
                    message.setError(XMPPError.from(XMPPError.Condition.bad_request, "Oops! I can't recognize the message"));
                    chat.sendMessage(message);
                } catch (final SmackException.NotConnectedException e) {
                    unableToSendMessage(e);
                }
                return;
            }
            final Command cmd;
            switch (arguments[0]){
                case GetAttributeCommand.NAME:
                    cmd = new GetAttributeCommand(attributes);
                    break;
                default:
                    cmd = new UnknownCommand(arguments[0]);
                    break;
            }
            //process command
            try {
                message = cmd.doCommand(ArrayUtils.remove(arguments, 0));
            }
            catch (final InvalidCommandFormatException e){
                message = new Message();
                message.setSubject("Invalid command format");
                message.setError(XMPPError.from(XMPPError.Condition.bad_request, e.getMessage()));
            }
            catch (final CommandException e){
                message = new Message();
                message.setSubject("Error in managed resource");
                message.setError(XMPPError.from(XMPPError.Condition.service_unavailable, e.getMessage()));
            }
            //send message to participant
            try {
                chat.sendMessage(message);
            } catch (final SmackException.NotConnectedException e) {
                unableToSendMessage(e);
            }
        }

        private void unableToSendMessage(final Exception e) {
            try (final OSGiLoggingContext context = OSGiLoggingContext.get(logger, getBundleContext())) {
                context.log(Level.SEVERE, "Unable to send XMPP message", e);
            }
        }
    }

    private final LinkedList<ChatSession> sessions;
    private final Logger logger;
    private final XMPPAttributeModel attributes;

    ChatController(final Logger logger){
        sessions = new LinkedList<>();
        this.logger = Objects.requireNonNull(logger);
        this.attributes = new XMPPAttributeModel();
    }

    @Override
    public void chatCreated(final Chat chat, final boolean createdLocally) {
        try (final LockScope ignored = beginWrite()) {
            sessions.add(new ChatSession(chat, attributes, logger));
        }
        sayHello(chat);
    }

    private void sayHello(final Chat chat){
        try {
            chat.sendMessage(String.format("Hi, %s!", chat.getParticipant()));
        }
        catch (final SmackException.NotConnectedException e) {
            try(final OSGiLoggingContext context = OSGiLoggingContext.get(logger, getBundleContext())){
                context.log(Level.WARNING, "Unable to send Hello message", e);
            }
        }
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextByObject(this);
    }

    private static String[] splitArguments(final String value){
        final List<String> matchList = new LinkedList<>();
        final Matcher regexMatcher = COMMAND_DELIMITER.matcher(value);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }
        return matchList.toArray(new String[matchList.size()]);
    }

    public XMPPAttributeModel getAttributes(){
        return attributes;
    }

    /**
     * Closes all chat sessions.
     */
    public void closeAllChats(){
        try(final LockScope ignored = beginWrite()){
            sessions.clear();
        }
    }

    /**
     * Releases all resources associated with this controller.
     */
    @Override
    public void close() {
        closeAllChats();
    }
}
