package com.snamp.hosting;

import com.snamp.PlatformService;
import com.snamp.ServiceProvider;

/**
 * Represents agent hosting context.
 * @author roman
 */
public interface HostingContext extends ServiceProvider {
    /**
     * Represents {@link Agent} reference.
     */
    static final Class<Agent> AGENT_SERVICE = Agent.class;

    /**
     * Represents {@link AgentConfigurationStorage} reference.
     */
    static final Class<AgentConfigurationStorage> CONFIG_STORAGE = AgentConfigurationStorage.class;


}
