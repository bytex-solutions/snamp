package com.bytex.snamp.supervision.discovery.http;

import com.bytex.snamp.supervision.discovery.ResourceDiscoveryService;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DiscoveryServiceServlet extends ServletContainer {
    static final String CONTEXT = ResourceDiscoveryService.HTTP_ENDPOINT;

    DiscoveryServiceServlet(){
        super(createAppConfig());
    }

    private static Application createAppConfig() {
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        result.getSingletons().add(new RESTDiscoveryService());
        return result;
    }
}
