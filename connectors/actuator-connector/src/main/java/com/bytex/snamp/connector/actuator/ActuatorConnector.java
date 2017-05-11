package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;

import java.net.URI;
import java.util.Map;

/**
 * Collects information from Spring Actuator endpoints including health information and metrics.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ActuatorConnector extends AbstractManagedResourceConnector {
    private final WebResource actuatorRoot;
    @Aggregation(cached = true)
    private final ActuatorAttributeRepository attributes;

    ActuatorConnector(final String resourceName,
                      final URI actuatorUri,
                      final ManagedResourceInfo configuration) {
        setConfiguration(configuration);
        final ActuatorConnectorDescriptionProvider provider = ActuatorConnectorDescriptionProvider.getInstance();
        final ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        final Client actuatorClient = Client.create(config);
        setupAuthentication(actuatorClient, provider, configuration);
        actuatorClient.setExecutorService(provider.parseThreadPool(configuration));
        actuatorRoot = actuatorClient.resource(actuatorUri);
        attributes = new ActuatorAttributeRepository(resourceName, actuatorRoot);
    }

    private static void setupAuthentication(final Client actuatorClient,
                                            final ActuatorConnectorDescriptionProvider provider,
                                            final Map<String, String> configuration) {
        final ClientFilter authFilter = provider.parseAuthentication(configuration);
        if (authFilter != null)
            actuatorClient.addFilter(authFilter);
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes);
    }

    @Override
    protected MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        attributes.close();
        super.close();
    }
}
