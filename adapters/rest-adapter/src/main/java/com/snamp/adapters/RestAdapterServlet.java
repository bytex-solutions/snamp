package com.snamp.adapters;

import com.snamp.connectors.util.AttributesRegistryReader;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.util.logging.Logger;

/**
 * Represents descriptor of the HTTP servlet container.
 * @author Roman Sakno
 */
final class RestAdapterServlet extends ServletContainer {
    private static Application createResourceConfig(final RestAdapterService serviceInstance){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(serviceInstance);
        return result;
    }

    /**
     * Initializes a new instance of the rest service.
     * @param attributes
     */
    public RestAdapterServlet(final AttributesRegistryReader attributes, final Logger serviceLogger){
        super(createResourceConfig(new RestAdapterService(attributes, serviceLogger)));
    }
}
