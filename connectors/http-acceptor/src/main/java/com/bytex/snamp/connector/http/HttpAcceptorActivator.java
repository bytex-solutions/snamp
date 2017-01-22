package com.bytex.snamp.connector.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.osgi.service.http.HttpService;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

/**
 * Represents activator of {@link HttpAcceptor}.
 */
public final class HttpAcceptorActivator extends ManagedResourceActivator<HttpAcceptor> {
    private static final String SERVLET_CONTEXT = "/snamp/data/acquisition";
    private static final ActivationProperty<HttpService> HTTP_SERVICE_ACTIVATION_PROPERTY = defineActivationProperty(HttpService.class);

    @SpecialUse
    public HttpAcceptorActivator() {
        super(HttpAcceptorActivator::newResourceConnector, configurationDescriptor(HttpConnectorConfigurationDescriptionProvider::getInstance));
    }

    private static HttpAcceptor newResourceConnector(final String resourceName,
                                                     final ManagedResourceInfo configuration,
                                              final DependencyManager dependencies) throws IOException{
        return new HttpAcceptor(resourceName, configuration);
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
        publisher.registerServlet(SERVLET_CONTEXT, new JerseyServletContainer(), new Hashtable<>(), null);
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) {
        final HttpService publisher = activationProperties.getProperty(HTTP_SERVICE_ACTIVATION_PROPERTY);
        publisher.unregister(SERVLET_CONTEXT);
    }
}
