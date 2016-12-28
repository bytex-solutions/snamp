package com.bytex.snamp.web;

import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.core.LoggingScope;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.web.JWTokenLocation;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import org.eclipse.jetty.websocket.servlet.*;
import org.osgi.framework.*;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents registry of all registered {@link WebConsoleService}s.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WebConsoleEngineImpl extends WebSocketServlet implements WebConsoleEngine, AutoCloseable, Constants, WebSocketCreator {
    static final String CONTEXT = "/snamp/console/events";
    private static final class WebConsoleServiceProcessingScope extends LoggingScope{
        private WebConsoleServiceProcessingScope(final WebConsoleEngine engine){
            super(engine, "processWebConsoleService");
        }

        private void failedToProcess(final ServiceEvent event, final Exception e){
            log(Level.SEVERE, String.format("Failed to process service for WebConsole. Service reference: %s, event: %s", event.getServiceReference(), event.getType()), e);
        }
    }
    private transient final AbstractConcurrentResourceAccessor<KeyedObjects<String, WebConsoleServiceReference>> services;
    private transient final WebSecurityFilter securityFilter;

    WebConsoleEngineImpl() {
        services = new ConcurrentResourceAccessor<>(AbstractKeyedObjects.create(WebConsoleServiceReference::getName));
        securityFilter = new WebSecurityFilter(JWTokenLocation.COOKIE, JWTokenLocation.AUTHORIZATION_HEADER);
        try {
            getBundleContext().addServiceListener(this, String.format("(%s=%s)", OBJECTCLASS, WebConsoleService.class.getName()));
        } catch (final InvalidSyntaxException e) {
            getBundleContext().addServiceListener(this);
        }
    }

    @Override
    public WebSocketChannel createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
        final Principal principal = callUnchecked(() -> {
            try {
                securityFilter.filter(req.getHttpServletRequest());
            } catch (final NoSuchAlgorithmException | IOException e) {
                resp.sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.toString());
                return null;
            } catch (final InvalidKeyException | SignatureException e) {
                resp.sendError(Response.Status.UNAUTHORIZED.getStatusCode(), e.getMessage());
                return null;
            }
            final Principal socketUser = WebSecurityFilter.getPrincipal(req.getHttpServletRequest());
            if (socketUser == null)
                resp.sendError(Response.Status.UNAUTHORIZED.getStatusCode(), "Authorization token required");
            return socketUser;
        });
        if (principal != null)
            return services.read(services -> {
                final WebSocketChannel channel = new WebSocketChannel(principal);
                services.values().forEach(holder -> holder.get().addWebEventListener(channel));
                return channel;
            });
        else
            return null;
    }

    @Override
    public void configure(final WebSocketServletFactory factory) {
        factory.setCreator(this);
        factory.getPolicy().setIdleTimeout(10_000);
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    private void serviceChanged(final int type, final ServiceReference<WebConsoleService> serviceRef) throws Exception {
        services.write(services -> {
            switch (type) {
                case ServiceEvent.MODIFIED_ENDMATCH:
                case ServiceEvent.UNREGISTERING:
                    final String name = WebConsoleServiceReference.getName(serviceRef);
                    WebConsoleServiceReference holder = services.remove(name);
                    if (holder != null)
                        holder.close();
                    break;
                case ServiceEvent.REGISTERED:
                    final WebConsoleServiceReference reference = WebConsoleServiceReference.isResourceModel(serviceRef) ?
                            new WebConsoleServiceServlet(getBundleContext(), serviceRef, securityFilter) :
                            new WebConsoleServiceHolder(getBundleContext(), serviceRef);
                    reference.activate();
                    services.put(reference);
                    break;
                case ServiceEvent.MODIFIED:

            }
            return null;
        });
    }

    @Override
    public Collection<WebConsoleService> registeredServices() {
        return services.read(services -> services.values().stream().map(WebConsoleServiceReference::get).collect(Collectors.toList()));
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (Utils.isInstanceOf(event.getServiceReference(), WebConsoleService.class)) {
            final WebConsoleServiceProcessingScope loggingScope = new WebConsoleServiceProcessingScope(this);
            try {
                serviceChanged(event.getType(), (ServiceReference<WebConsoleService>) event.getServiceReference());
            } catch (final Exception e){
                loggingScope.failedToProcess(event, e);
            } finally {
                loggingScope.close();
            }
        }
    }

    @Override
    public <T> T queryObject(@Nonnull final Class<T> objectType) {
        return objectType.isInstance(this) ? objectType.cast(this) : null;
    }

    @Override
    public void close() throws Exception {
        getBundleContext().removeServiceListener(this);
        services.write(services -> {
            for (final WebConsoleServiceReference service : services.values())
                service.close();
            services.clear();
            return null;
        });
    }
}
