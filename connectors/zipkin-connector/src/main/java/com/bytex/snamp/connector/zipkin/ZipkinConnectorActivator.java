package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.osgi.service.http.HttpService;

import java.net.URISyntaxException;
import java.util.Collection;
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

    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(new SimpleDependency<>(HttpService.class));
    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties) throws Exception {
        super.activate(activationProperties);
        @SuppressWarnings("unchecked")
        final HttpService publisher = getDependencies().getDependency(HttpService.class);
        assert publisher != null;
        activationProperties.publish(HTTP_SERVICE_ACTIVATION_PROPERTY, publisher);
        //register servlet
        publisher.registerServlet(ZipkinServlet.CONTEXT, new ZipkinServlet(), new Hashtable<>(), null);
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) {
        final HttpService publisher = activationProperties.getProperty(HTTP_SERVICE_ACTIVATION_PROPERTY);
        publisher.unregister(ZipkinServlet.CONTEXT);
    }


}
