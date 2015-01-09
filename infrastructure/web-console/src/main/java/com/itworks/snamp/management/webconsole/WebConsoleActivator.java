package com.itworks.snamp.management.webconsole;

import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.core.AbstractBundleActivator;
import com.itworks.snamp.management.SnampManager;
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
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WebConsoleActivator extends AbstractBundleActivator {
    private static final String WEB_CONSOLE_PORT = "com.itworks.snamp.webconsole.port";
    private static final String DEFAULT_WEB_CONSOLE_PORT = "3344";
    private static final String WEB_CONSOLE_HOST = "com.itworks.snamp.webconsole.host";
    private static final String DEFAULT_WEB_CONSOLE_HOST = "localhost";
    private static final String LOGIN_MODULE_NAME = "SNAMP_WEB_CONSOLE";
    private static final String REALM = "Snamp Web Console Security";

    private Server jettyServer = new Server();

    private static void removeConnectors(final Server jettyServer){
        for(final Connector c: jettyServer.getConnectors())
            if(c instanceof NetworkConnector) ((NetworkConnector)c).close();
        jettyServer.setConnectors(new Connector[0]);
    }

    private static LoginService createLoginService(final Class<?> callerClass){
        final JAASLoginService loginService = SecurityUtils.createJaasLoginServiceForOsgi(callerClass.getClassLoader());
        loginService.setLoginModuleName(LOGIN_MODULE_NAME);
        loginService.setName(REALM);
        loginService.setRoleClassNames(new String[]{JAASRole.class.getName()});
        return loginService;
    }

    private static int getWebConsolePort(final BundleContext context){
        String port = context.getProperty(WEB_CONSOLE_PORT);
        if(port == null || port.isEmpty())
            port = System.getProperty(WEB_CONSOLE_PORT, DEFAULT_WEB_CONSOLE_PORT);
        return Integer.parseInt(port);
    }

    private static String getWebConsoleHost(final BundleContext context){
        String host = context.getProperty(WEB_CONSOLE_HOST);
        if(host == null || host.isEmpty())
            host = System.getProperty(WEB_CONSOLE_HOST, DEFAULT_WEB_CONSOLE_HOST);
        return host;
    }

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    @Override
    protected void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        //remove all connectors.
        removeConnectors(jettyServer);
        //initializes a new connector.
        final ServerConnector connector = new ServerConnector(jettyServer);
        connector.setPort(getWebConsolePort(context));
        connector.setHost(getWebConsoleHost(context));
        jettyServer.setConnectors(new Connector[]{connector});
        //add dependencies
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationAdmin.class));
        bundleLevelDependencies.add(new SimpleDependency<>(SnampManager.class));
    }

    /**
     * Activates the bundle.
     * <p>
     * This method will be called when all bundle-level dependencies will be resolved.
     * </p>
     *
     * @param context              The execution context of the bundle being activated.
     * @param activationProperties A collection of bundle's activation properties to fill.
     * @param dependencies         A collection of resolved dependencies.
     * @throws Exception An exception occurred during activation.
     */
    @Override
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        final ServletContextHandler resourcesHandler = new ServletContextHandler(ServletContextHandler.SECURITY);
        resourcesHandler.setContextPath("/snamp");
        //security
        final ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        security.setCheckWelcomeFiles(true);
        final Constraint constraint = new Constraint();
        constraint.setAuthenticate(true);
        constraint.setName("snampauth");
        constraint.setRoles(new String[]{SecurityUtils.ADMIN_ROLE, SecurityUtils.USER_ROLE});
        security.setRealmName(REALM);
        security.addRole(SecurityUtils.ADMIN_ROLE);
        security.addRole(SecurityUtils.USER_ROLE);
        final ConstraintMapping cmapping = new ConstraintMapping();
        cmapping.setPathSpec("/management/*");
        cmapping.setConstraint(constraint);
        security.setConstraintMappings(new ConstraintMapping[]{cmapping});
        security.setLoginService(createLoginService(getClass()));
        security.setAuthenticator(new DigestAuthenticator());
        //Setup REST service
        final PersistentConfigurationManager manager = new PersistentConfigurationManager(getDependency(RequiredServiceAccessor.class, ConfigurationAdmin.class, dependencies));
        manager.load();
        resourcesHandler.addServlet(new ServletHolder(
                new ManagementServlet(
                        manager,
                        getDependency(RequiredServiceAccessor.class, SnampManager.class, dependencies))),
                "/management/api/*");
        //Setup static pages
        resourcesHandler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
        resourcesHandler.setInitParameter("org.eclipse.jetty.servlet.Default.pathInfoOnly", "true");
        final DefaultServlet staticPages = new StaticPagesServlet();
        resourcesHandler.addServlet(new ServletHolder(staticPages), "/management/console/*");
        resourcesHandler.setSecurityHandler(security);
        jettyServer.setHandler(resourcesHandler);
        jettyServer.addBean(security.getLoginService());
        jettyServer.start();
    }

    /**
     * Deactivates the bundle.
     * <p>
     * This method will be called when at least one bundle-level dependency will be lost.
     * </p>
     *
     * @param context              The execution context of the bundle being deactivated.
     * @param activationProperties A collection of activation properties to read.
     * @throws Exception An exception occurred during bundle deactivation.
     */
    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        jettyServer.stop();
        jettyServer.setHandler(null);
    }

    /**
     * Stops the bundle.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception An exception occurred during bundle stopping.
     */
    @Override
    protected void shutdown(final BundleContext context) throws Exception {
        removeConnectors(jettyServer);
    }
}
