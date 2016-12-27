package com.bytex.snamp.web;

import com.bytex.snamp.security.Anonymous;
import com.bytex.snamp.web.serviceModel.WebEvent;
import com.bytex.snamp.web.serviceModel.WebEventListener;
import com.google.common.base.MoreObjects;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.security.Principal;

/**
 * Represents duplex communication channel between WebConsole on browser and backend.
 */
final class WebSocketChannel extends WebSocketAdapter implements WebEventListener {
    private final Principal principal;

    WebSocketChannel(final Principal owner){
        this.principal = MoreObjects.firstNonNull(Anonymous.INSTANCE, owner);
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
