package com.itworks.snamp.adapters.rest;

import com.google.common.base.Supplier;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.EventBusManager;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.adapters.AbstractConcurrentResourceAdapter;
import com.itworks.snamp.configuration.ThreadPoolConfig;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import com.itworks.snamp.internal.Utils;
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
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.osgi.service.event.EventHandler;

import java.text.DateFormat;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Represents HTTP adapter that exposes management information through HTTP and WebSocket to the outside world.
 * This class cannot be inherited.
 * @author Roman Sakno
 */
final class RestAdapter extends AbstractConcurrentResourceAdapter {
    private static final String REALM_NAME = "SNAMP_REST_ADAPTER";
    static final String NAME = RestAdapterHelpers.ADAPTER_NAME;

    private static final class HttpNotifications extends AbstractNotificationsModel<HttpNotificationMapping> implements EventHandler, HttpNotificationsModel, AutoCloseable{
        private final Gson jsonFormatter;
        private final EventBus notificationBus;

        private HttpNotifications(final Gson jsonFormatter){
            this.jsonFormatter = jsonFormatter;
            this.notificationBus = new EventBus();
        }

        public EventBusManager.SubscriptionManager<JsonNotificationListener> getNotificationEmitter(){
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
            return jsonFormatter;
        }

        @Override
        public HttpNotificationMapping get(final String resourceName, final String userDefineEventName) {
            return get(makeSubscriptionListID(resourceName, userDefineEventName));
        }

        @Override
        public void close() {

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

        void setPort(final int value){
            port = value;
        }

        void setHost(final String value){
            host = value;
        }

        void setThreadPool(final ExecutorService value){
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
    private final JettyServerBuilder serverFactory;
    private final String loginModuleName;
    private final HttpAttributes attributes;
    private final int webSocketTimeout;
    private final HttpNotifications notifications;

    RestAdapter(final String adapterInstance,
                    final int port,
                       final String host,
                       final String loginModuleName,
                       final String dateFormat,
                       final int webSocketTimeout,
                       final ThreadPoolConfig threadPoolConfig){
        super(adapterInstance, threadPoolConfig);
        this.serverFactory = new JettyServerBuilder(host, port);
        this.loginModuleName = loginModuleName;
        this.webSocketTimeout = webSocketTimeout;
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

    private static LoginService createLoginService(final String loginModuleName, final Class<?> callerClass){
        final JAASLoginService loginService = RestAdapterHelpers.createJaasLoginServiceForOsgi(callerClass.getClassLoader());
        loginService.setLoginModuleName(loginModuleName);
        loginService.setName(REALM_NAME);
        loginService.setRoleClassNames(new String[]{JAASRole.class.getName()});
        return loginService;
    }

    /**
     * Starts the adapter.
     * @param threadPool The thread pool used by Web server.
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    protected void start(final ExecutorService threadPool) throws Exception{
        serverFactory.setThreadPool(threadPool);
        jettyServer = serverFactory.get();
        final boolean securityEnabled = loginModuleName != null && loginModuleName.length() > 0;
        final ServletContextHandler resourcesHandler = new ServletContextHandler(securityEnabled ? ServletContextHandler.SECURITY : ServletContextHandler.NO_SESSIONS);
        resourcesHandler.setContextPath("/snamp/managedResource");
        //security
        if(securityEnabled) {
            final ConstraintSecurityHandler security = new ConstraintSecurityHandler();
            security.setCheckWelcomeFiles(true);
            final Constraint constraint = new Constraint();
            constraint.setAuthenticate(true);
            constraint.setName("restadapterauth");
            constraint.setRoles(new String[]{RestAdapterHelpers.MAINTAINER_ROLE, RestAdapterHelpers.MONITOR_ROLE});
            security.setRealmName(REALM_NAME);
            security.addRole(RestAdapterHelpers.MAINTAINER_ROLE);
            security.addRole(RestAdapterHelpers.MONITOR_ROLE);
            final ConstraintMapping cmapping = new ConstraintMapping();
            cmapping.setPathSpec("/attributes/*");
            cmapping.setConstraint(constraint);
            security.setConstraintMappings(new ConstraintMapping[]{cmapping});
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
        Utils.withContextClassLoader(getClass().getClassLoader(), new ExceptionalCallable<Void, Exception>() {
            @Override
            public Void call() throws Exception {
                jettyServer.start();
                return null;
            }
        });
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
     * @param threadPool The thread pool used by Web server.
     */
    @Override
    protected void stop(final ExecutorService threadPool) throws Exception{
        try {
            jettyServer.stop();
        }
        finally {
            jettyServer.setHandler(null);
            serverFactory.setThreadPool(null);
            clearModel(attributes);
            clearModel(notifications);
            notifications.close();
            jettyServer = null;
        }
        System.gc();
    }
}
