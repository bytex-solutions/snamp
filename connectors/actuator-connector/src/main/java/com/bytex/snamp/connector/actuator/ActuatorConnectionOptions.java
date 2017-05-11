package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.SafeCloseable;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;

import java.net.URI;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ActuatorConnectionOptions implements SafeCloseable {
    private final WebResource healthResource;
    private final WebResource metricsResource;
    private final boolean smartMode;

    ActuatorConnectionOptions(final URI actuatorUri,
                              final Map<String, String> configuration){
        final ActuatorConnectorDescriptionProvider provider = ActuatorConnectorDescriptionProvider.getInstance();
        final ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        final Client actuatorClient = Client.create(config);
        setupAuthentication(actuatorClient, provider, configuration);
        actuatorClient.setExecutorService(provider.parseThreadPool(configuration));
        final WebResource actuatorRoot = actuatorClient.resource(actuatorUri);
        metricsResource = actuatorClient.resource(actuatorRoot.getUriBuilder().segment(provider.getMetricsPath(configuration)).build());
        healthResource = actuatorClient.resource(actuatorRoot.getUriBuilder().segment(provider.getHealthPath(configuration)).build());
        smartMode = provider.isSmartModeEnabled(configuration);
    }

    private static void setupAuthentication(final Client actuatorClient,
                                            final ActuatorConnectorDescriptionProvider provider,
                                            final Map<String, String> configuration) {
        final ClientFilter authFilter = provider.parseAuthentication(configuration);
        if (authFilter != null)
            actuatorClient.addFilter(authFilter);
    }

    WebResource getHealthResource(){
        return healthResource;
    }

    WebResource getMetricsResource(){
        return metricsResource;
    }

    boolean isSmartModeEnabled(){
        return smartMode;
    }

    @Override
    public void close() {

    }
}
