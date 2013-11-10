package com.snamp.restmanager;

import com.snamp.hosting.Agent;
import com.snamp.hosting.AgentConfiguration;
import com.snamp.hosting.AgentConfigurationStorage;
import com.snamp.hosting.HostingContext;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.*;
import java.io.IOException;

/**
 * @author Alexey Grishin
 * @version 1.0
 * @since 1.0
 */

@Path("/configuration")
@Singleton
public class RestManagerService {
    private final Agent agent;
    private final AgentConfigurationStorage storage;
    public RestManagerService(final Agent agent, final AgentConfigurationStorage storage){
        this.agent = agent;
        this.storage = storage;
    }

    @GET
    @Produces
    public String getConfiguration() throws IOException {
        final AgentConfigurationStorage.StoredAgentConfiguration storedConfig = storage.getStoredAgentConfiguration(AgentConfigurationStorage.TAG_LAST);
        AgentConfiguration config = storedConfig.restore();

        return "";
    }

}
