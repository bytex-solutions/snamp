package com.bytex.snamp.connector.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import java.io.IOException;
import java.util.Hashtable;

/**
 * Represents activator of {@link HttpAcceptor}.
 */
public final class HttpAcceptorActivator extends ManagedResourceActivator<HttpAcceptor> {
    private static final ActivationProperty<HttpService> HTTP_SERVICE_ACTIVATION_PROPERTY = defineActivationProperty(HttpService.class);

    @SpecialUse(SpecialUse.Case.OSGi)
    public HttpAcceptorActivator() {
        super(HttpAcceptorActivator::newResourceConnector, configurationDescriptor(HttpConnectorConfigurationDescriptionProvider::getInstance));
    }

    private static HttpAcceptor newResourceConnector(final String resourceName,
                                                     final ManagedResourceInfo configuration,
                                              final DependencyManager dependencies) throws IOException{
        return new HttpAcceptor(resourceName, configuration);
    }
    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    @Override
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) throws Exception {
        super.start(context, bundleLevelDependencies);
        bundleLevelDependencies.add(HttpService.class);
    }

    @Override
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final DependencyManager dependencies) throws Exception {
        super.activate(context, activationProperties, dependencies);
        final HttpService publisher = dependencies.getDependency(HttpService.class);
        assert publisher != null;
        activationProperties.publish(HTTP_SERVICE_ACTIVATION_PROPERTY, publisher);
        //register servlet
        publisher.registerServlet(JerseyServletContainer.CONTEXT, new JerseyServletContainer(), new Hashtable<>(), null);
    }

    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        try {
            activationProperties.getProperty(HTTP_SERVICE_ACTIVATION_PROPERTY).unregister(JerseyServletContainer.CONTEXT);
        }finally {
            super.deactivate(context, activationProperties);
        }
    }
}
