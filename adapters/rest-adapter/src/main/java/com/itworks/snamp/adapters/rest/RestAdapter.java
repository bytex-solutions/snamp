package com.itworks.snamp.adapters.rest;

import com.google.common.base.Supplier;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.Box;
import com.itworks.snamp.EventBusManager;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import com.itworks.snamp.internal.Utils;
import org.eclipse.jetty.jaas.JAASLoginService;
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
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.osgi.service.event.EventHandler;

import java.text.DateFormat;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.rest.RestAdapterConfigurationDescriptor.*;

/**
 * Represents HTTP adapter that exposes management information through HTTP and WebSocket to the outside world.
 * This class cannot be inherited.
 * @author Roman Sakno
 */
final class RestAdapter extends AbstractResourceAdapter {
    private static final String SECURITY_ALIAS = "SNAMP_REST_ADAPTER";
    static final String NAME = RestAdapterHelpers.ADAPTER_NAME;

    private static final class HttpNotifications extends AbstractNotificationsModel<HttpNotificationMapping> implements EventHandler, HttpNotificationsModel, AutoCloseable{
        private final Box<Gson> jsonFormatter;
        private final EventBus notificationBus;

        private HttpNotifications(){
            this.jsonFormatter = new Box<>(new Gson());
            this.notificationBus = new EventBus();
        }

        private EventBusManager.SubscriptionManager<JsonNotificationListener> getNotificationEmitter(){
            return EventBusManager.getSubscriptionManager(notificationBus);
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
            return new HttpNotificationMapping(notifMeta, jsonFormatter);
        }

        /**
         * Processes SNMP notification.
         * @param sender The name of the managed resource which emits the notification.
         * @param notif                The notification to process.
         * @param notificationMetadata The metadata of the notification.
         */
        @Override
        protected void handleNotification(final String sender, final Notification notif, final HttpNotificationMapping notificationMetadata) {
            notificationBus.post(new JsonNotification(notif, notificationMetadata.getCategory(), notificationMetadata));
        }

        @Override
        public Gson getJsonFormatter() {
            return jsonFormatter.get();
        }

        @Override
        public HttpNotificationMapping get(final String resourceName, final String userDefineEventName) {
            return get(makeSubscriptionListID(resourceName, userDefineEventName));
        }

        private void setDateTimeFormat(final String dateTimeFormat) {
            jsonFormatter.set(createJsonFormatter(dateTimeFormat));
        }

        @Override
        public void close() {

        }
    }

    private static final class HttpAttributes extends AbstractAttributesModel<HttpAttributeMapping> implements HttpAttributesModel {
        private final Box<Gson> jsonFormatter;

        private HttpAttributes() {
            jsonFormatter = new Box<>(new Gson());
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
            return jsonFormatter.get();
        }

        @Override
        public HttpAttributeMapping get(final String resourceName, final String userDefinedAttributeName) {
            return get(makeAttributeID(resourceName, userDefinedAttributeName));
        }

        private void setDateTimeFormat(final String dateTimeFormat) {
            jsonFormatter.set(createJsonFormatter(dateTimeFormat));
        }
    }

    private static final class JettyServerBuilder implements Supplier<Server> {
        private int port;
        private String host;
        private ExecutorService threadPool;

        private JettyServerBuilder(){
            this(RestAdapterConfigurationDescriptor.DEFAULT_HOST,
                    RestAdapterConfigurationDescriptor.DEFAULT_PORT);
        }

        private JettyServerBuilder(final String host, final int port) {
            this.port = port;
            this.host = host;
        }

        private void setPort(final int value){
            port = value;
        }

        private void setHost(final String value){
            host = value;
        }

        private void setThreadPool(final ExecutorService value){
            this.threadPool = value;
        }

        private static void removeConnectors(final Server jettyServer){
            for(final Connector c: jettyServer.getConnectors())
                if(c instanceof NetworkConnector) ((NetworkConnector)c).close();
            jettyServer.setConnectors(new Connector[0]);
        }

        @Override
        public Server get() {
            final Server result = threadPool != null ?
                    new Server(new ExecutorThreadPool(threadPool)):
                    new Server();
            //remove all connectors.
            removeConnectors(result);
            //initializes a new connector.
            final ServerConnector connector = new ServerConnector(result);
            connector.setPort(port);
            connector.setHost(host);
            result.setConnectors(new Connector[]{connector});
            return result;
        }
    }

    private Server jettyServer;
    private final HttpAttributes attributes;
    private final HttpNotifications notifications;

    RestAdapter(final String adapterInstance){
        super(adapterInstance);
        attributes = new HttpAttributes();
        notifications = new HttpNotifications();
    }

    private static Gson createJsonFormatter(final String dateFormat){
        final GsonBuilder builder = new GsonBuilder();
        if(dateFormat == null || dateFormat.isEmpty())
            builder.setDateFormat(DateFormat.FULL);
        else builder.setDateFormat(dateFormat);
        builder.serializeNulls();
        return builder.create();
    }

    private static LoginService createLoginService(final String realmName, final Class<?> callerClass){
        final JAASLoginService loginService = RestAdapterHelpers.createJaasLoginServiceForOsgi(realmName, callerClass.getClassLoader());
        loginService.setName(SECURITY_ALIAS);
        loginService.setRoleClassNames(JAASLoginService.DEFAULT_ROLE_CLASS_NAMES);
        return loginService;
    }

    private void start(final Supplier<Server> serverFactory,
                       final String realmName,
                       final String dateTimeFormat,
                       final int webSocketTimeout) throws Exception {
        final Server jettyServer = serverFactory.get();
        attributes.setDateTimeFormat(dateTimeFormat);
        notifications.setDateTimeFormat(dateTimeFormat);
        final boolean securityEnabled = realmName != null && realmName.length() > 0;
        final ServletContextHandler resourcesHandler = new ServletContextHandler(securityEnabled ? ServletContextHandler.SECURITY : ServletContextHandler.NO_SESSIONS);
        resourcesHandler.setContextPath("/snamp/managedResource");
        //security
        if (securityEnabled) {
            final ConstraintSecurityHandler security = new ConstraintSecurityHandler();
            security.setCheckWelcomeFiles(true);
            final Constraint constraint = new Constraint();
            constraint.setAuthenticate(true);
            constraint.setName("restadapterauth");
            constraint.setRoles(new String[]{RestAdapterHelpers.MAINTAINER_ROLE, RestAdapterHelpers.MONITOR_ROLE});
            security.setRealmName(SECURITY_ALIAS);
            security.addRole(RestAdapterHelpers.MAINTAINER_ROLE);
            security.addRole(RestAdapterHelpers.MONITOR_ROLE);
            final ConstraintMapping cmapping = new ConstraintMapping();
            cmapping.setPathSpec("/attributes/*");
            cmapping.setConstraint(constraint);
            security.setConstraintMappings(new ConstraintMapping[]{cmapping});
            security.setLoginService(createLoginService(realmName, getClass()));
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
        this.jettyServer = Utils.withContextClassLoader(getClass().getClassLoader(), new ExceptionalCallable<Server, Exception>() {
            @Override
            public Server call() throws Exception {
                jettyServer.start();
                return jettyServer;
            }
        });
    }

    private void start(final int port,
                       final String hostName,
                       final String realmName,
                       final String dateTimeFormat,
                       final int webSocketTimeout,
                       final Supplier<ExecutorService> threadPoolFactory) throws Exception{
        final JettyServerBuilder serverBuilder = new JettyServerBuilder(hostName, port);
        serverBuilder.setThreadPool(threadPoolFactory.get());
        start(serverBuilder, realmName, dateTimeFormat, webSocketTimeout);
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception{
        final Supplier<ExecutorService> threadPoolFactory = new JettyThreadPoolConfig(parameters,
                getInstanceName());
        RestAdapterLimitations.current().verifyServiceVersion(RestAdapter.class);
        final String port = parameters.containsKey(PORT_PARAM) ?
                parameters.get(PORT_PARAM) : Integer.toString(DEFAULT_PORT);
        final String host = parameters.containsKey(HOST_PARAM) ?
                parameters.get(HOST_PARAM) :
                DEFAULT_HOST;
        final String socketTimeout = parameters.containsKey(WEB_SOCKET_TIMEOUT_PARAM) ?
                parameters.get(WEB_SOCKET_TIMEOUT_PARAM) :
                Integer.toString(DEFAULT_TIMEOUT);
        start(Integer.valueOf(port),
                host,
                parameters.get(REALM_NAME_PARAM),
                parameters.get(DATE_FORMAT_PARAM),
                Integer.valueOf(socketTimeout),
                threadPoolFactory);
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }

    /**
     * Stops the adapter.
     */
    @Override
    protected void stop() throws Exception {
        try {
            jettyServer.stop();
        } finally {
            final ExecutorService threadPool = jettyServer.getBean(ExecutorService.class);
            if (threadPool != null)
                threadPool.shutdownNow();
            jettyServer.setHandler(null);
            clearModel(attributes);
            clearModel(notifications);
            notifications.close();
            jettyServer = null;
        }
        System.gc();
    }

    /**
     * Invokes when resource connector is in stopping state or resource configuration was removed.
     * <p>
     * This method will be called automatically by SNAMP infrastructure.
     * In the default implementation this method throws internal exception
     * derived from {@link UnsupportedOperationException} indicating
     * that the adapter should be restarted.
     * It is recommended to use {@link #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel)}
     * and/or {@link #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractNotificationsModel)} to
     * update your underlying models.
     * </p>
     *
     * @param resourceName The name of the resource to be removed.
     * @see #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel)
     * @see #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractNotificationsModel)
     */
    @Override
    protected void resourceRemoved(final String resourceName) {
        clearModel(resourceName, attributes);
        clearModel(resourceName, notifications);
    }

    /**
     * Invokes when a new resource connector is activated or new resource configuration is added.
     * <p/>
     * This method will be called automatically by SNAMP infrastructure.
     * In the default implementation this method throws internal exception
     * derived from {@link UnsupportedOperationException} indicating
     * that the adapter should be restarted.
     * </p
     *
     * @param resourceName The name of the resource to be added.
     * @see #enlargeModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel)
     */
    @Override
    protected void resourceAdded(final String resourceName) {
        try {
            enlargeModel(resourceName, attributes);
            enlargeModel(resourceName, notifications);
        }
        catch (final Exception e){
            RestAdapterHelpers.log(Level.SEVERE, String.format("Unable to process new resource %s. Restarting adapter %s.", resourceName, NAME), e);
            super.resourceAdded(resourceName);
        }
    }
}
