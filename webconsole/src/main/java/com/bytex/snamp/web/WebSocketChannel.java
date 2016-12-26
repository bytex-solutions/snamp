package com.bytex.snamp.web;

import com.bytex.snamp.security.web.JWTAuthenticator;
import com.bytex.snamp.web.serviceModel.WebEvent;
import com.bytex.snamp.web.serviceModel.WebEventListener;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.security.Principal;
import java.util.Objects;

/**
 * Represents duplex communication channel between WebConsole on browser and backend.
 */
final class WebSocketChannel extends WebSocketAdapter implements WebEventListener {
    private final Principal principal;

    WebSocketChannel(final Principal owner){
        this.principal = Objects.requireNonNull(owner);
    }

    @Override
    public void accept(final WebEvent event) {

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
