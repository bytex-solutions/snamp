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
final class WebSocketChannel extends WebSocketAdapter implements WebEventListener, WriteCallback {
    private final Principal principal;
    private final ObjectMapper jsonSerializer;

    WebSocketChannel(final Principal owner){
        this.principal = Objects.requireNonNull(owner);
        jsonSerializer = new ObjectMapper();
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
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketConnect(final Session sess) {
        getLogger().info(() -> String.format("Established WebSocket connection with %s", sess.getRemoteAddress()));
        super.onWebSocketConnect(sess);
    }

    @Override
    public void writeFailed(final Throwable e) {
        getLogger().log(Level.WARNING, "Failed to write data into WebSocket", e);
    }

    @Override
    public void writeSuccess() {

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
                getRemote().sendString(serializedEvent, this);
        }
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }
}
