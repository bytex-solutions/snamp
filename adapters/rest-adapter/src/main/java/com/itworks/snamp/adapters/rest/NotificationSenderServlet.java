package com.itworks.snamp.adapters.rest;

import com.google.gson.Gson;
import com.itworks.snamp.internal.semantics.Internal;
import net.engio.mbassy.bus.common.PubSubSupport;
import net.engio.mbassy.listener.Invoke;
import net.engio.mbassy.listener.Listener;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.*;

import javax.servlet.annotation.WebServlet;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    @Listener
    private static final class NotificationSender implements WebSocketListener{
        private final Gson jsonFormatter;
        private static final Logger log = RestAdapterHelpers.getLogger();
        private volatile Reference<Session> currentSession;

        public NotificationSender(final Gson jsonFormatter){
            this.jsonFormatter = jsonFormatter;
            currentSession = null;
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
            this.currentSession = new WeakReference<>(session);
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
            if(currentSession != null){
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

        private static void processNotification(final Session webSocket,
                                                final JsonNotification notification,
                                                final Gson jsonFormatter){
            if(webSocket.isOpen()){
                //send message asynchronously
                webSocket.getRemote().sendStringByFuture(notification.toString(jsonFormatter));
            }
        }

        @net.engio.mbassy.listener.Handler(delivery = Invoke.Asynchronously)
        public void processNotification(final JsonNotification notification){
            final Session session = currentSession != null ? currentSession.get() : null;
            if(session != null) processNotification(session, notification, jsonFormatter);
        }
    }

    private static final class NotificationSenderFactory implements WebSocketCreator {
        private final PubSubSupport<JsonNotification> subscriptionManager;
        private final Gson jsonFormatter;

        public NotificationSenderFactory(final PubSubSupport<JsonNotification> subscriber, final Gson jsonFormatter) {
            this.subscriptionManager = subscriber;
            this.jsonFormatter = jsonFormatter;
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
            final String TEXT_SUBP = "text";
            if(req.getSubProtocols().contains(TEXT_SUBP)){
                resp.setAcceptedSubProtocol(TEXT_SUBP);
                final NotificationSender sender = new NotificationSender(jsonFormatter);
                subscriptionManager.subscribe(sender);
                return sender;
            }
            else return null;
        }
    }

    private final PubSubSupport<JsonNotification> subscriber;
    private final Gson jsonFormatter;
    private final int webSocketIdleTimeout;

    public NotificationSenderServlet(final PubSubSupport<JsonNotification> subscriber,
                                     final Gson jsonFormatter,
                                     final int idleTimeout){
        this.subscriber = subscriber;
        this.webSocketIdleTimeout = idleTimeout;
        this.jsonFormatter = jsonFormatter;
    }

    @Override
    public final void configure(final WebSocketServletFactory factory) {
        factory.setCreator(new NotificationSenderFactory(subscriber, jsonFormatter));
        factory.getPolicy().setIdleTimeout(webSocketIdleTimeout);
    }
}
