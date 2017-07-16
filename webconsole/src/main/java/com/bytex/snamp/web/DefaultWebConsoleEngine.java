package com.bytex.snamp.web;

import com.bytex.snamp.Convert;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.DefaultServiceSelector;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.web.JWTAuthFilter;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import org.eclipse.jetty.websocket.servlet.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents registry of all registered {@link WebConsoleService}s.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DefaultWebConsoleEngine extends WebSocketServlet implements WebConsoleEngine, WebSocketCreator {
    static final String CONTEXT = "/snamp/console/events";
    private transient final JWTAuthFilter securityFilter;

    DefaultWebConsoleEngine(final ClusterMember clusterMember) {
        securityFilter = WebConsoleSecurityFilter.forWebSocket(clusterMember);
    }

    private ServiceReference<WebConsoleService>[] getServices() {
        return new DefaultServiceSelector()
                .setServiceType(WebConsoleService.class)
                .getServiceReferences(getBundleContext(), WebConsoleService.class);
    }

    private WebSocketChannel sendError(final ServletUpgradeResponse resp,
                                       final Response.Status status,
                                       final String message) {
        try {
            resp.sendError(status.getStatusCode(), message);
        } catch (final IOException e) {
            getLogger().log(Level.SEVERE, "Failed to abort upgrade request", e);
        }
        return null;
    }

    @Override
    public WebSocketChannel createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
        try {
            securityFilter.filter(req.getHttpServletRequest());
        } catch (final NoSuchAlgorithmException | IOException e) {
            return sendError(resp, Response.Status.INTERNAL_SERVER_ERROR, e.toString());
        } catch (final InvalidKeyException | SignatureException e) {
            return sendError(resp, Response.Status.UNAUTHORIZED, e.getMessage());
        }
        final Principal principal = JWTAuthFilter.getPrincipal(req.getHttpServletRequest());
        if (principal == null) {
            return sendError(resp, Response.Status.UNAUTHORIZED, "Authorization token required");
        } else {
            //attach web socket channel to each service
            final WebSocketChannel channel = new WebSocketChannel(principal);
            for (final ServiceReference<WebConsoleService> serviceRef : getServices()) {
                final WebConsoleService service = getBundleContext().getService(serviceRef);
                if (service != null)
                    try {
                        service.attachSession(channel);
                    } finally {
                        getBundleContext().ungetService(serviceRef);
                    }
            }
            return channel;
        }
    }

    @Override
    public void configure(final WebSocketServletFactory factory) {
        factory.setCreator(this);
        factory.getPolicy().setIdleTimeout(10_000);
    }

    private BundleContext getBundleContext() {
        return Utils.getBundleContextOfObject(this);
    }

    private Logger getLogger() {
        return LoggerProvider.getLoggerForBundle(getBundleContext());
    }

    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        return Convert.toType(this, objectType);
    }
}
