package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import java.net.URISyntaxException;
import java.util.Hashtable;

/**
 * Collects spans compatible with Twitter Zipkin.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public final class ZipkinConnectorActivator extends ManagedResourceActivator<ZipkinConnector> {
    private static final ActivationProperty<HttpService> HTTP_SERVICE_ACTIVATION_PROPERTY = defineActivationProperty(HttpService.class);

    @SpecialUse(SpecialUse.Case.OSGi)
    public ZipkinConnectorActivator(){
        super(ZipkinConnectorActivator::createConnector, configurationDescriptor(ZipkinConnectorConfigurationDescriptionProvider::getInstance));
    }

    private static ZipkinConnector createConnector(final String resourceName,
                                                   final ManagedResourceInfo configuration,
                                                   final DependencyManager dependencies) throws URISyntaxException {
        return new ZipkinConnector(resourceName, configuration);
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
        @SuppressWarnings("unchecked")
        final HttpService publisher = dependencies.getDependency(HttpService.class);
        assert publisher != null;
        activationProperties.publish(HTTP_SERVICE_ACTIVATION_PROPERTY, publisher);
        //register servlet
        publisher.registerServlet(ZipkinServlet.CONTEXT, new ZipkinServlet(), new Hashtable<>(), null);
    }

    /**
     * Deactivates this library.
     * <p>
     * This method will be invoked when at least one dependency was lost.
     * </p>
     *
     * @param context              The execution context of the library being deactivated.
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Deactivation error.
     */
    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        try {
            activationProperties.getProperty(HTTP_SERVICE_ACTIVATION_PROPERTY).unregister(ZipkinServlet.CONTEXT);
        } finally {
            super.deactivate(context, activationProperties);
        }
    }
}
