package com.bytex.snamp.gateway.http;

import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.ModelOfNotifications;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents collection of connected events.
 */
final class HttpModelOfNotifications extends ModelOfNotifications<HttpNotificationAccessor> implements WebSocketCreator, NotificationListener {
    private class NotificationChannel extends WebSocketAdapter implements NotificationListener, WriteCallback{
        @Override
        public final void onWebSocketConnect(final Session session) {
            super.onWebSocketConnect(session);
            webSocketListeners.add(this);
        }

        boolean isAllowed(final Notification notification){
            return true;
        }

        @Override
        public final void handleNotification(final NotificationEvent event) {
            if (isConnected() && isAllowed(event.getNotification())) {
                getRemote().sendString(formatter.toJson(event.getNotification()), this);
            }
        }

        @Override
        public void writeFailed(final Throwable e) {
            getLogger().log(Level.WARNING, "Failed to send notification", e);
        }

        @Override
        public void writeSuccess() {

        }

        @Override
        public final void onWebSocketError(final Throwable cause) {
            getLogger().log(Level.WARNING, "WebSocket error detected", cause);
        }

        @Override
        public final void onWebSocketClose(final int statusCode, final String reason) {
            getLogger().fine(() -> String.format("WebSocket is closed with status %s (%s)", statusCode, reason));
            super.onWebSocketClose(statusCode, reason);
            webSocketListeners.remove(this);
        }
    }

    private final class FilteredNotificationChannel extends NotificationChannel implements NotificationListener {
        private final String resourceName;

        FilteredNotificationChannel(final String resourceName){
            this.resourceName = resourceName;
        }

        @Override
        boolean isAllowed(final Notification notification) {
            return Objects.equals(resourceName, notification.getSource());
        }
    }

    private final Pattern slashReplace = Pattern.compile("/", Pattern.LITERAL);
    private final Gson formatter = JsonUtils.registerTypeAdapters(new GsonBuilder())
            .serializeSpecialFloatingPointValues()
            .serializeNulls()
            .create();

    private final Set<NotificationListener> webSocketListeners;

    HttpModelOfNotifications() {
        webSocketListeners = new CopyOnWriteArraySet<>();
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    protected HttpNotificationAccessor createAccessor(final String resourceName, final MBeanNotificationInfo metadata) throws Exception {
        return new HttpNotificationAccessor(resourceName, metadata, this);
    }

    @Override
    public WebSocketListener createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
        String resourceName = req.getHttpServletRequest().getPathInfo();
        resourceName = slashReplace.matcher(resourceName).replaceAll("");
        return isNullOrEmpty(resourceName) ? new NotificationChannel() : new FilteredNotificationChannel(resourceName);
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
