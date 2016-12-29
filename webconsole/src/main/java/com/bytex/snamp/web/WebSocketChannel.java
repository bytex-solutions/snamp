package com.bytex.snamp.web;

import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.web.serviceModel.WebEvent;
import com.bytex.snamp.web.serviceModel.WebEventListener;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WriteCallback;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents duplex communication channel between WebConsole on browser and backend.
 */
final class WebSocketChannel extends WebSocketAdapter implements WebEventListener {
    private final Principal principal;
    private final ObjectMapper jsonSerializer;
    private final WriteCallback callback;

    WebSocketChannel(final Principal owner){
        this.principal = Objects.requireNonNull(owner);
        jsonSerializer = new ObjectMapper();
        callback = createCallback(getLogger());
    }

    private static WriteCallback createCallback(final Logger logger){
        return new WriteCallback() {
            @Override
            public void writeFailed(final Throwable e) {
                logger.log(Level.WARNING, "Failed to write data into WebSocket", e);
            }

            @Override
            public void writeSuccess() {

            }
        };
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    public void onWebSocketError(final Throwable e) {
        getLogger().log(Level.WARNING, "Failed to transmit data over WebSocket", e);
    }

    @Override
    public void onWebSocketClose(final int statusCode, final String reason) {
        getLogger().info(() -> String.format("WebSocket is closed with status %s(%s)", statusCode, reason));
    }

    @Override
    public void onWebSocketConnect(final Session sess) {
        getLogger().info(() -> String.format("WebSocket connection %s is established", sess.getRemoteAddress()));
    }

    @Override
    public void accept(final WebEvent event) {
        if (isConnected()) {
            final String serializedEvent;
            try (final StringWriter writer = new StringWriter(1024)) {
                jsonSerializer.writeValue(writer, event);
                serializedEvent = writer.toString();
            } catch (final IOException e) {
                getLogger().log(Level.SEVERE, String.format("Unable to serialize event %s into JSON", event), e);
                return;
            }
            if (isConnected())
                getRemote().sendString(serializedEvent, callback);
        }
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }
}
