package com.itworks.snamp.management.webconsole;

import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.core.AbstractBundleActivator;
import com.itworks.snamp.management.SnampManager;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.BundleContext;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WebConsoleActivator extends AbstractBundleActivator {
    private static final String WEB_CONSOLE_PORT = "com.itworks.snamp.webconsole.port";
    private static final String WEB_CONSOLE_HOST = "com.itworks.snamp.webconsole.host";
    private Server jettyServer = new Server();

    private static void removeConnectors(final Server jettyServer){
        for(final Connector c: jettyServer.getConnectors())
            if(c instanceof NetworkConnector) ((NetworkConnector)c).close();
        jettyServer.setConnectors(new Connector[0]);
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
        final int port = Integer.valueOf(context.getProperty(WEB_CONSOLE_PORT));
        String host = context.getProperty(WEB_CONSOLE_HOST);
        if(host == null || host.isEmpty()) host = "127.0.0.1";
        //remove all connectors.
        removeConnectors(jettyServer);
        //initializes a new connector.
        final ServerConnector connector = new ServerConnector(jettyServer);
        connector.setPort(port);
        connector.setHost(host);
        jettyServer.setConnectors(new Connector[]{connector});
        //add dependencies
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationManager.class));
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
        final ServletContextHandler resourcesHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        resourcesHandler.setContextPath("/snamp");
        //Setup REST service
        resourcesHandler.addServlet(new ServletHolder(
                new ManagementServlet(
                        getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies),
                        getDependency(RequiredServiceAccessor.class, SnampManager.class, dependencies))),
                "/console/*");
        jettyServer.setHandler(resourcesHandler);
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
