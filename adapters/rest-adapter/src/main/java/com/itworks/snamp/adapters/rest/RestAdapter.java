package com.itworks.snamp.adapters.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import com.itworks.snamp.internal.Utils;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.common.PubSubSupport;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.jaas.JAASRole;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.osgi.service.event.EventHandler;

import java.text.DateFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.ArrayUtils.toArray;

/**
 * Represents HTTP adapter that exposes management information through HTTP and WebSocket to the outside world.
 * This class cannot be inherited.
 * @author Roman Sakno
 */
final class RestAdapter extends AbstractResourceAdapter {
    private static final String REALM_NAME = "SNAMP_REST_ADAPTER";

    private static final class HttpNotifications extends AbstractNotificationsModel<HttpNotificationMapping> implements EventHandler, HttpNotificationsModel, AutoCloseable{
        private final Gson jsonFormatter;
        private final MBassador<JsonNotification> notificationBus;

        public HttpNotifications(final Gson jsonFormatter){
            this.jsonFormatter = jsonFormatter;
            this.notificationBus = new MBassador<>(BusConfiguration.Default());
        }

        public PubSubSupport<JsonNotification> getNotificationEmitter(){
            return notificationBus;
        }

        /**
         * Creates a new notification metadata representation.
         *
         * @param resourceName User-defined name of the managed resource.
         * @param eventName    The resource-local identifier of the event.
         * @param notifMeta    The notification metadata to wrap.
         * @return A new notification metadata representation.
         */
        @Override
        protected HttpNotificationMapping createNotificationView(final String resourceName, final String eventName, final NotificationMetadata notifMeta) {
            return new HttpNotificationMapping(notifMeta);
        }

        /**
         * Processes SNMP notification.
         * @param sender The name of the managed resource which emits the notification.
         * @param notif                The notification to process.
         * @param notificationMetadata The metadata of the notification.
         */
        @Override
        protected void handleNotification(final String sender, final Notification notif, final HttpNotificationMapping notificationMetadata) {
            notificationBus.publishAsync(new JsonNotification(notif, notificationMetadata.getCategory()));
        }

        @Override
        public Gson getJsonFormatter() {
            return jsonFormatter;
        }

        @Override
        public HttpNotificationMapping get(final String resourceName, final String userDefineEventName) {
            return get(makeSubscriptionListID(resourceName, userDefineEventName));
        }

        @Override
        public void close() {
            notificationBus.shutdown();
        }
    }

    private static final class HttpAttributes extends AbstractAttributesModel<HttpAttributeMapping> implements HttpAttributesModel {
        private final Gson jsonFormatter;

        public HttpAttributes(final Gson formatter){
            this.jsonFormatter = formatter;
        }

        /**
         * Creates a new domain-specific representation of the management attribute.
         *
         * @param resourceName             User-defined name of the managed resource.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @param accessor                 An accessor for the individual management attribute.
         * @return A new domain-specific representation of the management attribute.
         */
        @Override
        protected HttpAttributeMapping createAttributeView(final String resourceName, final String userDefinedAttributeName, final AttributeAccessor accessor) {
            return new HttpAttributeMapping(accessor, jsonFormatter);
        }

        /**
         * Creates a new unique identifier of the management attribute.
         * <p>
         * The identifier must be unique through all instances of the resource adapter.
         * </p>
         *
         * @param resourceName             User-defined name of the managed resource which supply the attribute.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @return A new unique identifier of the management attribute.
         */
        @Override
        protected String makeAttributeID(final String resourceName, final String userDefinedAttributeName) {
            return String.format("%s/%s", resourceName, userDefinedAttributeName);
        }

        @Override
        public Gson getJsonFormatter() {
            return jsonFormatter;
        }

        @Override
        public HttpAttributeMapping get(final String resourceName, final String userDefinedAttributeName) {
            return get(makeAttributeID(resourceName, userDefinedAttributeName));
        }
    }

    private final Server jettyServer;
    private final String loginModuleName;
    private final HttpAttributes attributes;
    private final int webSocketTimeout;
    private final HttpNotifications notifications;

    public RestAdapter(final int port,
                       final String host,
                       final String loginModuleName,
                       final String dateFormat,
                       final int webSocketTimeout,
                       final Map<String, ManagedResourceConfiguration> resources){
        super(resources);
        this.jettyServer = new Server();
        this.loginModuleName = loginModuleName;
        this.webSocketTimeout = webSocketTimeout;
        //remove all connectors.
        removeConnectors(jettyServer);
        //initializes a new connector.
        final ServerConnector connector = new ServerConnector(jettyServer);
        connector.setPort(port);
        connector.setHost(host);
        jettyServer.setConnectors(toArray(connector));
        final Gson jsonFormatter = createJsonFormatter(dateFormat);
        attributes = new HttpAttributes(jsonFormatter);
        notifications = new HttpNotifications(jsonFormatter);
    }

    private static Gson createJsonFormatter(final String dateFormat){
        final GsonBuilder builder = new GsonBuilder();
        if(dateFormat == null || dateFormat.isEmpty())
            builder.setDateFormat(DateFormat.FULL);
        else builder.setDateFormat(dateFormat);
        builder.serializeNulls();
        return builder.create();
    }

    private static void removeConnectors(final Server jettyServer){
        for(final Connector c: jettyServer.getConnectors())
            if(c instanceof NetworkConnector) ((NetworkConnector)c).close();
        jettyServer.setConnectors(new Connector[0]);
    }

    private static LoginService createLoginService(final String loginModuleName, final Class<?> callerClass){
        final JAASLoginService loginService = RestAdapterHelpers.createJaasLoginServiceForOsgi(callerClass.getClassLoader());
        loginService.setLoginModuleName(loginModuleName);
        loginService.setName(REALM_NAME);
        loginService.setRoleClassNames(toArray(JAASRole.class.getName()));
        return loginService;
    }

    /**
     * Starts the adapter.
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    protected boolean start() {
        final ServletContextHandler resourcesHandler = new ServletContextHandler(ServletContextHandler.SECURITY);
        resourcesHandler.setContextPath("/snamp/managedResource");
        //security
        final boolean securityEnabled;
        if(securityEnabled = loginModuleName != null && loginModuleName.length() > 0) {
            final ConstraintSecurityHandler security = new ConstraintSecurityHandler();
            security.setCheckWelcomeFiles(true);
            final Constraint constraint = new Constraint();
            constraint.setAuthenticate(true);
            constraint.setName("restadapterauth");
            constraint.setRoles(toArray(RestAdapterHelpers.MAINTAINER_ROLE, RestAdapterHelpers.MONITOR_ROLE));
            security.setRealmName(REALM_NAME);
            security.addRole(RestAdapterHelpers.MAINTAINER_ROLE);
            security.addRole(RestAdapterHelpers.MONITOR_ROLE);
            final ConstraintMapping cmapping = new ConstraintMapping();
            cmapping.setPathSpec("/attributes/*");
            cmapping.setConstraint(constraint);
            security.setConstraintMappings(toArray(cmapping));
            security.setLoginService(createLoginService(loginModuleName, getClass()));
            security.setAuthenticator(new DigestAuthenticator());
            resourcesHandler.setSecurityHandler(security);
            jettyServer.addBean(security.getLoginService());
        }
        //Setup REST service
        resourcesHandler.addServlet(new ServletHolder(
                        new RestAdapterServlet(attributes, securityEnabled)),
                "/attributes/*");
        //notification delivery
        resourcesHandler.addServlet(new ServletHolder(new NotificationSenderServlet(notifications.getNotificationEmitter(),
                notifications.getJsonFormatter(),
                webSocketTimeout)), "/notifications/*");
        jettyServer.setHandler(resourcesHandler);
        populateModel(attributes);
        populateModel(notifications);
        final MutableBoolean result = new MutableBoolean(false);
        Utils.withContextClassLoader(getClass().getClassLoader(),  new Runnable() {
            @Override
            public void run() {
                try {
                    jettyServer.start();
                    result.setValue(true);
                }
                catch (final Exception e) {
                    getLogger().log(Level.SEVERE, "Unable to start embedded HTTP server", e);
                }
            }
        });
        return result.getValue();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return RestAdapterHelpers.getLogger();
    }

    /**
     * Stops the adapter.
     */
    @Override
    protected void stop() {
        try {
            jettyServer.stop();
        }
        catch (final Exception e) {
            getLogger().log(Level.SEVERE, "Unable to stop embedded HTTP server.", e);
        }
        finally {
            clearModel(attributes);
            clearModel(notifications);
            jettyServer.setHandler(null);
        }
    }

    /**
     * Releases all resources associated with this adapter.
     * <p>
     * You should call base implementation of this method
     * in the overridden method.
     * </p>
     *
     * @throws Exception An exception occurred during adapter releasing.
     */
    @Override
    public void close() throws Exception {
        try {
            jettyServer.stop();
        }
        finally {
            notifications.close();
            super.close();
        }
    }
}
