package com.snamp.restmanager;

import com.snamp.connectors.util.AttributesRegistryReader;
import com.snamp.hosting.Agent;
import com.snamp.hosting.AgentConfigurationStorage;
import com.snamp.hosting.HostingContext;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.util.logging.Logger;

/**
 * @author Alexey Grishin
 * @version 1.0
 * @since 1.0
 */
public class RestAdapterManagerServlet extends ServletContainer {
    private static Application createResourceConfig(final RestManagerService serviceInstance){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(serviceInstance);
        return result;
    }

    /**
     * Initializes a new instance of the rest service.
     * @param agent
     * @param storage
     */
    public RestAdapterManagerServlet(final Agent agent, final AgentConfigurationStorage storage){
        super(createResourceConfig(new RestManagerService(agent, storage)));
    }
}
