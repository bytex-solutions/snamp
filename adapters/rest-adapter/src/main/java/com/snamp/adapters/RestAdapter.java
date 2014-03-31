package com.snamp.adapters;

import com.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.snamp.configuration.RestAdapterConfigurationDescriptor;
import com.snamp.connectors.*;
import com.snamp.connectors.util.*;
import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;
import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.snamp.configuration.RestAdapterConfigurationDescriptor.*;

import com.snamp.configuration.AgentConfiguration;
import com.snamp.licensing.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.meta.Author;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.*;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Represents HTTP adapter that exposes management information through HTTP and WebSocket to the outside world.
 * This class cannot be inherited.
 * @author Roman Sakno
 */
@PluginImplementation
@Author(name = "Roman Sakno")
final class RestAdapter extends AbstractAdapter implements LicensedPlatformPlugin<RestAdapterLimitations>, NotificationPublisher {
    public static final String NAME = "rest";
    private final Server jettyServer;
    private final AttributesRegistry exposedAttributes;
    private final SubscriptionList notifications;
    private boolean started = false;
    private ConfigurationEntityDescriptionProvider configDescr;

    public RestAdapter(){
        super(NAME);
        RestAdapterLimitations.current().verifyPluginVersion(getClass());
        jettyServer = new Server();
        exposedAttributes = new AbstractAttributesRegistry<AttributeConfiguration>() {
            @Override
            protected ConnectedAttributes<AttributeConfiguration> createBinding(final AttributeSupport connector) {
                return new ConnectedAttributes<AttributeConfiguration>(connector) {
                    @Override
                    public final String makeAttributeId(final String prefix, final String postfix) {
                        return RestAdapterHelpers.makeAttributeID(prefix, postfix);
                    }

                    @Override
                    public final AttributeConfiguration createDescription(final String prefix, final String postfix, final AttributeConfiguration config) {
                        return config;
                    }
                };
            }
        };
        started = false;
        notifications = new SubscriptionList();
    }

    private Servlet createRestServlet(final String dateFormat){
        return new RestAdapterServlet(dateFormat, exposedAttributes, getLogger());
    }

    private final boolean initializeServer(final Server s, final Map<String, String> parameters){
        final int port = parameters.containsKey(PORT_PARAM_NAME) ? Integer.valueOf(parameters.get(PORT_PARAM_NAME)) : 8080;
        final String host = parameters.containsKey(ADDRESS_PARAM_NAME) ? parameters.get(ADDRESS_PARAM_NAME) : "0.0.0.0";
        final String dateFormat = parameters.containsKey(DATE_FORMAT_PARAM_NAME) ? parameters.get(DATE_FORMAT_PARAM_NAME) : "";
        final int webSocketIdleTimeout = Integer.valueOf(parameters.containsKey(WEB_SOCKET_TIMEOUT_PARAM_NAME) ? parameters.get(WEB_SOCKET_TIMEOUT_PARAM_NAME) : "10000");
        //remove all connectors.
        for(final Connector c: s.getConnectors())
            if(c instanceof NetworkConnector) ((NetworkConnector)c).close();
        s.setConnectors(new Connector[0]);
        //initializes a new connector.
        final ServerConnector connector = new ServerConnector(s);
        connector.setPort(port);
        connector.setHost(host);
        s.setConnectors(new Connector[]{connector});
        final ServletContextHandler resourcesHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        resourcesHandler.setContextPath("/snamp");
        //notification delivery
        resourcesHandler.addServlet(new ServletHolder(new NotificationSenderServlet(notifications.createSubscriptionManager(), getLogger(), dateFormat, webSocketIdleTimeout)), "/notifications/*");
        //attribute getters and setters
        resourcesHandler.addServlet(new ServletHolder(createRestServlet(dateFormat)), "/management/*");
        s.setHandler(resourcesHandler);
        return true;
    }

    @Aggregation
    public final ConfigurationEntityDescriptionProvider getConfigurationDescriptor(){
        if(configDescr == null)
            configDescr = new RestAdapterConfigurationDescriptor();
        return configDescr;
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
                if(!saveState){
                    exposedAttributes.disconnect();
                    exposedAttributes.clear();
                    notifications.disable();
                }
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
     * Releases all resources associated with this adapter.
     * @throws Exception Some error occurs during resource releasing.
     */
    @Override
    public final void close() throws Exception {
        jettyServer.stop();
        exposedAttributes.disconnect();
        exposedAttributes.clear();
        notifications.disable();
        notifications.clear();
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

    /**
     * Exposes monitoring events.
     *
     * @param connector The management connector that provides notification listening and subscribing.
     * @param namespace The events namespace.
     * @param events    The collection of configured notifications.
     */
    @Override
    public final void exposeEvents(final NotificationSupport connector, final String namespace, final Map<String, EventConfiguration> events) {
        notifications.putAll(connector, namespace, events);
    }
}
