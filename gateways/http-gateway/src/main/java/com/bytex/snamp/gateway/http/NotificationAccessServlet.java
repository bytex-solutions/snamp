package com.bytex.snamp.gateway.http;

import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.Objects;

/**
 * Represents configuration of WebSocket used for notifications.
 */
final class NotificationAccessServlet extends WebSocketServlet {
    private static final long serialVersionUID = 2311053773928779779L;
    private final WebSocketCreator creator;

    NotificationAccessServlet(final WebSocketCreator creator){
        this.creator = Objects.requireNonNull(creator);
    }

    @Override
    public void configure(final WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(10_000);
        factory.setCreator(creator);
    }
}
