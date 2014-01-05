package com.snamp.adapters;

import com.snamp.connectors.AttributeSupport;
import com.snamp.connectors.util.*;
import com.snamp.hosting.AgentConfiguration;
import com.snamp.licensing.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.meta.Author;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.*;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Roman Sakno
 */
@PluginImplementation
@Author(name = "Roman Sakno")
final class RestAdapter extends AbstractAdapter implements LicensedPlatformPlugin<RestAdapterLimitations> {
    private static final String DATE_FORMAT_PARAM_NAME = "dateFormat";
    public static final String NAME = "rest";
    private final Server jettyServer;
    private final AttributesRegistry exposedAttributes;
    private boolean started = false;


    public RestAdapter(){
        super(NAME);
        RestAdapterLimitations.current().verifyPluginVersion(getClass());
        jettyServer = new Server();
        exposedAttributes = new AbstractAttributesRegistry() {
            @Override
            protected ConnectedAttributes createBinding(final AttributeSupport connector) {
                return new ConnectedAttributes(connector) {
                    @Override
                    public String makeAttributeId(final String prefix, final String postfix) {
                        return String.format("%s/%s", prefix, postfix);
                    }
                };
            }
        };
        started = false;
    }

    private Servlet createRestServlet(final String dateFormat){
        return new RestAdapterServlet(dateFormat, exposedAttributes, getLogger());
    }

    private final boolean initializeServer(final Server s, final Map<String, String> parameters){
        final int port = parameters.containsKey(PORT_PARAM_NAME) ? Integer.valueOf(parameters.get(PORT_PARAM_NAME)) : 8080;
        final String host = parameters.containsKey(ADDRESS_PARAM_NAME) ? parameters.get(ADDRESS_PARAM_NAME) : "0.0.0.0";
        final String dateFormat = parameters.containsKey(DATE_FORMAT_PARAM_NAME) ? parameters.get(DATE_FORMAT_PARAM_NAME) : "";
        //remove all connectors.
        for(final Connector c: s.getConnectors())
            if(c instanceof NetworkConnector) ((NetworkConnector)c).close();
        s.setConnectors(new Connector[0]);
        //initializes a new connector.
        final ServerConnector connector = new ServerConnector(s);
        connector.setPort(port);
        connector.setHost(host);
        s.setConnectors(new Connector[]{connector});
        final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        s.setHandler(contextHandler);
        contextHandler.setContextPath("/snamp/management");
        final ServletHolder holder = new ServletHolder(createRestServlet(dateFormat));
        contextHandler.addServlet(holder, "/*");
        return true;
    }

    /**
     * Exposes the connector to the world.
     *
     * @param parameters The adapter startup parameters.
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    public boolean start(final Map<String, String> parameters) throws IOException {
        if(!started && initializeServer(jettyServer, parameters))
            try {
                jettyServer.start();
                return started = true;
            }
            catch (final Exception e) {
                getLogger().log(Level.WARNING, e.getLocalizedMessage(), e);
                throw new IOException(e);
            }
        else return false;
    }

    /**
     * Stops the connector hosting.
     *
     * @param saveState {@literal true} to save previously exposed attributes for reuse; otherwise,
     *                       clear internal list of exposed attributes.
     * @return {@literal true}, if adapter is previously started; otherwise, {@literal false}.
     */
    @Override
    public boolean stop(final boolean saveState) {
        if(started)
            try {
                jettyServer.stop();
                started = false;
                return true;
            }
            catch (final Exception e) {
                getLogger().log(Level.WARNING, e.getLocalizedMessage(), e);
                return false;
            }
        else return false;
    }

    /**
     * Exposes management attributes.
     *
     * @param connector  Management connector that provides access to the specified attributes.
     * @param namespace  The attributes namespace.
     * @param attributes The dictionary of attributes.
     */
    @Override
    public final void exposeAttributes(final AttributeSupport connector, final String namespace, final Map<String, AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration> attributes) {
        exposedAttributes.putAll(connector, namespace, attributes);
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p/>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p/>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p/>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p/>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p/>
     * <p>Note that unlike the {@link java.io.Closeable#close close}
     * method of {@link java.io.Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p/>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public final void close() throws Exception {
        jettyServer.stop();
        exposedAttributes.clear();
        started = false;
    }

    /**
     * Returns license limitations associated with this plugin.
     *
     * @return The license limitations applied to this plugin.
     */
    @Override
    public final RestAdapterLimitations getLimitations() {
        return RestAdapterLimitations.current();
    }
}
