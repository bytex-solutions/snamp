package com.bytex.snamp.web;

import com.bytex.snamp.web.serviceModel.WebEvent;
import com.bytex.snamp.web.serviceModel.WebEventListener;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WriteCallback;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Objects;

/**
 * Represents duplex communication channel between WebConsole on browser and backend.
 */
final class WebSocketChannel extends WebSocketAdapter implements WebEventListener {
    private static final WriteCallback EMPTY_CALLBACK = new WriteCallback() {
        @Override
        public void writeFailed(final Throwable x) {

        }

        @Override
        public void writeSuccess() {

        }
    };

    private final Principal principal;
    private final ObjectMapper jsonSerializer;

    WebSocketChannel(final Principal owner){
        this.principal = Objects.requireNonNull(owner);
        jsonSerializer = new ObjectMapper();
    }

    @Override
    public void accept(final WebEvent event) {
        if (isConnected()) {
            final String serializedEvent;
            try (final StringWriter writer = new StringWriter(1024)) {
                jsonSerializer.writeValue(writer, event);
                serializedEvent = writer.toString();
            } catch (final IOException ignored) {
                //do not LOG here because LogNotifier may cause cyclic re-sending of logs
                return;
            }
            if (isConnected())
                getRemote().sendString(serializedEvent, EMPTY_CALLBACK);
        }
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public void onWebSocketText(final String message) {
        super.onWebSocketText(message);
    }
}
