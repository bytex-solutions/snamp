package com.snamp.adapters;

import com.google.gson.*;
import static com.snamp.connectors.NotificationSupport.Notification;
import static com.snamp.connectors.NotificationSupport.NotificationListener;
import static com.snamp.connectors.util.AbstractSubscriptionList.Subscription;

import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.servlet.*;

import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.*;

/**
 * Represents web socket factory that is used to deliver notifications to the client.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationSenderServlet extends WebSocketServlet {

    @WebSocket
    private static final class NotificationSender{
        private final SubscriptionManager subscriber;
        private final Collection<Subscription<?>> subscriptions;
        private final Logger log;
        private final Gson jsonFormatter;

        public NotificationSender(final SubscriptionManager subscriber, final Gson jsonFormatter, final Logger log){
            this.subscriber = subscriber;
            subscriptions = new ArrayList<>(10);
            this.log = log;
            this.jsonFormatter = jsonFormatter;
        }

        private static JsonObject serializeNotification(final Notification n){
            final JsonObject result = new JsonObject();
            result.add("message", new JsonPrimitive(n.getMessage()));
            return result;
        }

        private static NotificationListener createListener(final Session webSocket, final Gson jsonFormatter, final Logger log){
            return new NotificationListener() {
                @Override
                public final boolean handle(final Notification n) {
                    if(webSocket.isOpen())
                        try{
                            webSocket.getRemote().sendString(jsonFormatter.toJson(serializeNotification(n)));
                            return true;
                        }
                        catch (final IOException e){
                            log.log(Level.WARNING, "Unable to send notification via Web Socket", e);
                            return false;
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
        @OnWebSocketConnect
        public synchronized void subscribe(final Session connection) {
            subscriptions.addAll(subscriber.subscribeToAll(createListener(connection, jsonFormatter, log)).values());
        }

        /**
         * Called when an established websocket connection closes
         *
         * @param closeCode
         * @param message
         */
        @OnWebSocketClose
        public synchronized void unsubscribe(final Session connection, final int closeCode, final String message) {
            for(final Subscription<?> sub: subscriptions)
                sub.unsubscribe();
            subscriptions.clear();
            connection.close(closeCode, message);
            log.info(String.format("Web Socket is closed with code %s (%s)", closeCode, message));

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

    public NotificationSenderServlet(final SubscriptionManager manager, final Logger log, final String dateFormat){
        this.subscriber = manager;
        this.log = log;
        final GsonBuilder builder = new GsonBuilder();
        if(dateFormat == null || dateFormat.isEmpty())
            builder.setDateFormat(DateFormat.FULL);
        else builder.setDateFormat(dateFormat);
        builder.serializeNulls();
        jsonFormatter = builder.create();
    }

    @Override
    public final void configure(final WebSocketServletFactory factory) {
        factory.setCreator(new NotificationSenderFactory(subscriber, jsonFormatter, log));
    }
}
