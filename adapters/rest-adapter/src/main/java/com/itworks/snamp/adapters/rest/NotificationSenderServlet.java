package com.itworks.snamp.adapters.rest;

import com.google.gson.*;
import static com.itworks.snamp.connectors.NotificationSupport.Notification;
import static com.itworks.snamp.connectors.NotificationSupport.NotificationListener;
import static com.itworks.snamp.connectors.util.AbstractSubscriptionList.Subscription;

import com.itworks.snamp.internal.Internal;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.servlet.*;

import javax.servlet.annotation.WebServlet;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.*;

/**
 * Represents web socket factory that is used to deliver notifications to the client.
 * This class cannot be inherited.
 * <p>This class must be public to be accessible from Jetty infrastructure using Reflection.</p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@WebServlet
@Internal
final class NotificationSenderServlet extends WebSocketServlet {
    /**
     * Represents notification sender that uses Web Socket session to send the notifications.
     * This class cannot be inherited.
     * <p>This class must be public to be accessible from Jetty infrastructure using Reflection.</p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Internal
    private static final class NotificationSender implements WebSocketListener{
        private final SubscriptionManager subscriber;
        private final Collection<Subscription<?>> subscriptions;
        private final Logger log;
        private final Gson jsonFormatter;
        private volatile Reference<Session> currentSession;

        public NotificationSender(final SubscriptionManager subscriber, final Gson jsonFormatter, final Logger log){
            this.subscriber = subscriber;
            subscriptions = new ArrayList<>(10);
            this.log = log;
            this.jsonFormatter = jsonFormatter;
            currentSession = null;
        }

        private static NotificationListener createListener(final Session webSocket, final Gson jsonFormatter, final Logger log){
            return new NotificationListener() {
                @Override
                public final boolean handle(final Notification n, final String category) {
                    if(webSocket.isOpen()){
                            final JsonNotification notification = new JsonNotification(n, category);
                            //send message asynchronously
                            webSocket.getRemote().sendStringByFuture(notification.toString(jsonFormatter));
                            return true;
                        }
                    else return false;
                }
            };
        }

        /**
         * Called when a new websocket connection is accepted.
         *
         * @param connection The Connection object to use to send messages.
         */
        public void subscribe(final Session connection) {
            synchronized (subscriber){
                subscriptions.addAll(subscriber.subscribeToAll(createListener(connection, jsonFormatter, log)).values());
            }
            currentSession = new WeakReference<>(connection);
        }

        /**
         * A WebSocket {@link org.eclipse.jetty.websocket.api.Session} has connected successfully and is ready to be used.
         * <p/>
         * Note: It is a good idea to track this session as a field in your object so that you can write messages back via the {@link org.eclipse.jetty.websocket.api.RemoteEndpoint}
         *
         * @param session the websocket session.
         */
        @Override
        public void onWebSocketConnect(final Session session) {
            subscribe(session);
        }

        /**
         * Called when an established websocket connection closes
         *
         * @param closeCode
         * @param message
         */
        public void unsubscribe(final Session connection, final int closeCode, final String message) {
            synchronized (subscriber){
                for(final Subscription<?> sub: subscriptions)
                    sub.unsubscribe();
                subscriptions.clear();
                connection.close(closeCode, message);
                log.info(String.format("Web Socket is closed with code %s (%s)", closeCode, message));
            }
        }

        /**
         * A Close Event was received.
         * <p/>
         * The underlying Connection will be considered closed at this point.
         *
         * @param statusCode the close status code. (See {@link org.eclipse.jetty.websocket.api.StatusCode})
         * @param reason     the optional reason for the close.
         */
        @Override
        public void onWebSocketClose(final int statusCode, final String reason) {
            if(currentSession != null)
            try{
                unsubscribe(currentSession.get(), statusCode, reason);
            }
            finally {
                currentSession.clear();
                currentSession = null;
            }
        }

        /**
         * A WebSocket exception has occurred.
         * <p/>
         * This is a way for the internal implementation to notify of exceptions occured during the processing of websocket.
         * <p/>
         * Usually this occurs from bad / malformed incoming packets. (example: bad UTF8 data, frames that are too big, violations of the spec)
         * <p/>
         * This will result in the {@link org.eclipse.jetty.websocket.api.Session} being closed by the implementing side.
         *
         * @param cause the error that occurred.
         */
        @Override
        public final void onWebSocketError(final Throwable cause) {
            log.log(Level.WARNING, "WebSocket error occured", cause);
            onWebSocketClose(StatusCode.SERVER_ERROR, cause.getMessage());
        }

        /**
         * A WebSocket Text frame was received.
         *
         * @param message
         */
        @Override
        public final void onWebSocketText(final String message) {
            log.info(String.format("Input text message %s is ignored", message));
        }

        /**
         * A WebSocket binary frame has been received.
         *
         * @param payload the raw payload array received
         * @param offset  the offset in the payload array where the data starts
         * @param len     the length of bytes in the payload
         */
        @Override
        public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
            log.info(String.format("Input binary message %s is ignored", Arrays.toString(payload)));
        }
    }

    private static final class NotificationSenderFactory implements WebSocketCreator {
        private final SubscriptionManager subscriptionManager;
        private final Gson jsonFormatter;
        private final Logger log;

        public NotificationSenderFactory(final SubscriptionManager subscriber, final Gson jsonFormatter, final Logger log) {
            this.subscriptionManager = subscriber;
            this.jsonFormatter = jsonFormatter;
            this.log = log;
        }

        /**
         * Create a websocket from the incoming request.
         *
         * @param req  the request details
         * @param resp the response details
         * @return a websocket object to use, or null if no websocket should be created from this request.
         */
        @Override
        public NotificationSender createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
            for(final String subprot: req.getSubProtocols())
                switch (subprot){
                    case "text":
                        resp.setAcceptedSubProtocol(subprot);
                        return new NotificationSender(subscriptionManager, jsonFormatter, log);
                    default: return null;
                }
            return null;
        }
    }

    private final SubscriptionManager subscriber;
    private final Logger log;
    private final Gson jsonFormatter;
    private final int webSocketIdleTimeout;

    public NotificationSenderServlet(final SubscriptionManager manager, final Logger log, final String dateFormat, final int idleTimeout){
        this.subscriber = manager;
        this.log = log;
        final GsonBuilder builder = new GsonBuilder();
        if(dateFormat == null || dateFormat.isEmpty())
            builder.setDateFormat(DateFormat.FULL);
        else builder.setDateFormat(dateFormat);
        builder.serializeNulls();
        jsonFormatter = builder.create();
        this.webSocketIdleTimeout = idleTimeout;
    }

    @Override
    public final void configure(final WebSocketServletFactory factory) {
        factory.setCreator(new NotificationSenderFactory(subscriber, jsonFormatter, log));
        factory.getPolicy().setIdleTimeout(webSocketIdleTimeout);
    }
}
