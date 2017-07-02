package com.bytex.snamp.web;

import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import com.bytex.snamp.web.serviceModel.WebMessage;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WriteCallback;

import java.io.*;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents duplex communication channel between WebConsole on browser and backend.
 */
final class WebSocketChannel extends WebSocketAdapter implements WebConsoleSession, WriteCallback {
    private static final String TYPE_PROPERTY = JsonTypeInfo.Id.NAME.getDefaultPropertyName();
    private final Principal principal;
    private final ObjectMapper jsonSerializer;
    /*
     *  Value may be of type JSONNode or already deserialized object
     */
    private final ConcurrentMap<String, Object> userData;

    WebSocketChannel(final Principal owner){
        principal = Objects.requireNonNull(owner);
        jsonSerializer = new ObjectMapper();
        userData = new ConcurrentHashMap<>();
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
        userData.clear();
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
    public void sendMessage(final WebMessage message) {
        if (isConnected()) {
            final String serializedEvent;
            try (final StringWriter writer = new StringWriter(1024)) {
                jsonSerializer.writeValue(writer, message);
                serializedEvent = writer.toString();
            } catch (final IOException e) {
                getLogger().log(Level.SEVERE, String.format("Unable to serialize event %s into JSON", message), e);
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

    @Override
    public void onWebSocketText(final String message) {
        try (final Reader reader = new StringReader(message)) {
            final JsonNode messageNode = jsonSerializer.readTree(reader);
            final JsonNode messageType = messageNode.get(TYPE_PROPERTY);
            if (messageType == null || !messageType.isTextual())
                getLogger().warning(String.format("Message %s has not property %s", message, TYPE_PROPERTY));
            else
                userData.put(messageType.getTextValue(), messageNode);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <USERDATA> Optional<USERDATA> getUserData(final Class<USERDATA> userDataType) {
        if (userDataType.isAnnotationPresent(JsonTypeName.class)) {
            final String typeName = userDataType.getAnnotation(JsonTypeName.class).value();
            Object userData = this.userData.get(typeName);
            if (userData == null)
                return Optional.empty();
            else if (userData instanceof JsonNode) try {
                final USERDATA typedData = jsonSerializer.readValue((JsonNode) userData, userDataType);
                this.userData.replace(typeName, userData, typedData);
                return Optional.of(typedData);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
            else
                return Optional.of(userData).map(userDataType::cast);
        } else
            return Optional.empty();
    }
}
