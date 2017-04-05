package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.core.LoggingScope;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.ExpressionBasedDescriptorFilter;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.XMPPError;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents XMPP Bot responsible for handling humand commands
 * and managing chat sessions.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class Bot implements ChatManagerListener, AutoCloseable {
    private static final Pattern COMMAND_DELIMITER = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

    private static final class MessageLoggingScope extends LoggingScope {
        private MessageLoggingScope(final AutoCloseable requester, final String messageType) {
            super(requester, messageType);
        }

        private void unableToSendMessage(final Exception e) {
            log(Level.WARNING, "Unable to send XMPP message", e);
        }
    }

    private static final class ChatSession<A extends AttributeReader & AttributeWriter> extends WeakReference<Chat> implements ChatMessageListener, Closeable, SafeCloseable, NotificationListener{
        private final A attributes;
        private volatile boolean closed;
        private final AtomicReference<ExpressionBasedDescriptorFilter> notificationFilter;

        private ChatSession(final Chat chat,
                            final A attributes){
            super(chat);
            chat.addMessageListener(this);
            this.attributes = attributes;
            notificationFilter = new AtomicReference<>(null);
        }

        @Override
        public void processMessage(final Chat chat, Message message) {
            if (message.getBody() == null) return;
            else if(closed){
                chat.removeMessageListener(this);
                return;
            }
            final String[] arguments = splitArguments(message.getBody());
            MessageLoggingScope loggingScope;
            if (arguments.length == 0) {
                loggingScope = new MessageLoggingScope(this, "processEmptyMessage");
                try {
                    message = new Message();
                    message.setSubject("Empty request");
                    message.setError(XMPPError.from(XMPPError.Condition.bad_request, "Oops! I can't recognize the message"));
                    chat.sendMessage(message);
                } catch (final SmackException.NotConnectedException e) {
                    loggingScope.unableToSendMessage(e);
                } finally {
                    loggingScope.close();
                }
                return;
            }
            final Command cmd;
            switch (arguments[0]) {
                case GetAttributeCommand.NAME:
                    cmd = new GetAttributeCommand(attributes);
                    break;
                case SetAttributeCommand.NAME:
                    cmd = new SetAttributeCommand(attributes);
                    break;
                case HelpCommand.NAME:
                    cmd = new HelpCommand();
                    break;
                case ExitCommand.NAME:
                    cmd = new ExitCommand(chat);
                    closed = true;
                    break;
                case ListOfResourcesCommand.NAME:
                    cmd = new ListOfResourcesCommand(attributes);
                    break;
                case ListOfAttributesCommand.NAME:
                    cmd = new ListOfAttributesCommand(attributes);
                    break;
                case ManageNotificationsCommand.NAME:
                    cmd = new ManageNotificationsCommand(notificationFilter);
                    break;
                default:
                    cmd = new UnknownCommand(arguments[0]);
                    break;
            }
            //process command
            loggingScope = new MessageLoggingScope(this, "doCommand");
            try {
                message = cmd.doCommand(ArrayUtils.remove(arguments, 0));
            } catch (final InvalidCommandFormatException e) {
                message = new Message();
                message.setSubject("Invalid command format");
                message.setError(XMPPError.from(XMPPError.Condition.bad_request, e.getMessage()));
            } catch (final CommandException e) {
                message = new Message();
                message.setSubject("Error in managed resource");
                message.setError(XMPPError.from(XMPPError.Condition.service_unavailable, e.getMessage()));
            } finally {
                loggingScope.close();
            }
            //send message to participant
            if (message != null) {
                loggingScope = new MessageLoggingScope(this, "sendResponse");
                try {
                    chat.sendMessage(message);
                } catch (final SmackException.NotConnectedException e) {
                    loggingScope.unableToSendMessage(e);
                } finally {
                    loggingScope.close();
                }
            }
        }

        private boolean isClosed(){
            return closed;
        }


        @Override
        public void close() {
            final Chat chat = get();
            if (chat != null) chat.close();
            clear();
            closed = true;
        }

        /**
         * Handles notifications.
         *
         * @param event Notification event.
         */
        @Override
        public void handleNotification(final NotificationEvent event) {
            final ExpressionBasedDescriptorFilter filter = notificationFilter.get();
            if(filter == null || filter.match(event.getSource())) {
                final Chat chat = get();
                if(chat == null) return;
                final Message message = new Message();
                message.setSubject("Notification");
                message.setBody(XMPPNotificationAccessor.toString(event.getNotification()));
                final Collection<ExtensionElement> extras = new LinkedList<>();
                XMPPNotificationAccessor.createExtensions(event.getSource(), extras);
                message.addExtensions(extras);
                try {
                    chat.sendMessage(message);
                } catch (final SmackException.NotConnectedException ignored) {
                    close();
                }
            }
        }
    }

    private final LinkedList<ChatSession> sessions;
    private final XMPPModelOfAttributes attributes;
    private final XMPPModelOfNotifications notifications;

    Bot(final ExecutorService threadPool){
        sessions = new LinkedList<>();
        this.attributes = new XMPPModelOfAttributes();
        this.notifications = new XMPPModelOfNotifications(threadPool);
    }

    private synchronized ChatSession chatCreatedImpl(final Chat chat){
        //remove closed sessions
        final Iterator<ChatSession> it = sessions.iterator();
        while (it.hasNext())
            if (it.next().isClosed()) it.remove();
        //register a new session
        final ChatSession session = new ChatSession<>(chat, attributes);
        sessions.add(session);
        return session;
    }

    @Override
    public void chatCreated(final Chat chat, final boolean createdLocally) {
        final ChatSession session = chatCreatedImpl(chat);
        notifications.addNotificationListener(session);
        sayHello(chat);
    }

    private void sayHello(final Chat chat) {
        final MessageLoggingScope logger = new MessageLoggingScope(this, "sayHello");
        try {
            chat.sendMessage(String.format("Hi, %s!", chat.getParticipant()));
        } catch (final SmackException.NotConnectedException e) {
            logger.unableToSendMessage(e);
        } finally {
            logger.close();
        }
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

    XMPPModelOfAttributes getAttributes(){
        return attributes;
    }

    XMPPModelOfNotifications getNotifications(){
        return notifications;
    }


    /**
     * Releases all resources associated with this controller.
     */
    @Override
    public void close() throws Exception {
        try {
            Utils.closeAll(sessions.toArray(new ChatSession<?>[0]));
        } finally {
            sessions.clear();
            attributes.clear();
            notifications.clear();
        }
    }
}
