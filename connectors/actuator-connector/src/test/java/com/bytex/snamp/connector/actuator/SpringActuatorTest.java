package com.bytex.snamp.connector.actuator;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SpringActuatorTest extends Assert {
    @Test
    public void requestMetrics(){
        final ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        final Client client = Client.create(config);
        final JsonNode result = client.resource("http://localhost:9233/metrics.json").get(JsonNode.class);
        assertNotNull(result);
    }
}
