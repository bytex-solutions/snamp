package com.bytex.snamp.gateway.http;

import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.ModelOfNotifications;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import javax.management.MBeanNotificationInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents collection of connected events.
 */
final class HttpModelOfNotifications extends ModelOfNotifications<HttpNotificationAccessor> implements WebSocketCreator, NotificationListener {
    private final class NotificationChannel extends WebSocketAdapter implements NotificationListener {
        private final String resourceName;

        NotificationChannel(final String resourceName){
            this.resourceName = resourceName;
        }

        @Override
        public void onWebSocketConnect(final Session sess) {
            super.onWebSocketConnect(sess);
            webSocketListeners.add(this);
        }

        @Override
        public void handleNotification(final NotificationEvent event) {
            if (isConnected() && Objects.equals(resourceName, event.getNotification().getSource())) {
                try {
                    getRemote().sendString(formatter.toJson(event.getNotification()));
                } catch (final IOException e) {
                    logger.log(Level.WARNING, String.format("Failed to send notification %s from %s", event.getNotification(), Arrays.toString(event.getSource().getNotifTypes())));
                }
            }
        }

        @Override
        public void onWebSocketError(final Throwable cause) {
            logger.log(Level.WARNING, "WebSocket error detected", cause);
        }

        @Override
        public void onWebSocketClose(final int statusCode, final String reason) {
            logger.fine(() -> String.format("WebSocket is closed with status %s (%s)", statusCode, reason));
            super.onWebSocketClose(statusCode, reason);
            webSocketListeners.remove(this);
        }
    }

    private final Gson formatter = JsonUtils.registerTypeAdapters(new GsonBuilder())
            .serializeSpecialFloatingPointValues()
            .serializeNulls()
            .create();

    private final Logger logger;
    private final Set<NotificationListener> webSocketListeners;

    HttpModelOfNotifications(final Logger logger) {
        this.logger = Objects.requireNonNull(logger);
        webSocketListeners = new CopyOnWriteArraySet<>();
    }

    @Override
    protected HttpNotificationAccessor createAccessor(final String resourceName, final MBeanNotificationInfo metadata) throws Exception {
        return new HttpNotificationAccessor(resourceName, metadata, this);
    }

    @Override
    public WebSocketListener createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
        return new NotificationChannel(req.getRequestPath());
    }

    @Override
    public void handleNotification(final NotificationEvent event) {
        webSocketListeners.forEach(listener -> listener.handleNotification(event));
    }

    @Override
    public void clear() {
        webSocketListeners.clear();
        super.clear();
    }
}
