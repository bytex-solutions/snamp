package com.snamp.hosting;

import com.snamp.Aggregator;

/**
 * Represents agent hosting context.
 * @author roman
 */
public interface HostingContext extends Aggregator {
    /**
     * Represents {@link Agent} reference.
     */
    static final Class<Agent> AGENT_SERVICE = Agent.class;

    /**
     * Represents {@link AgentConfigurationStorage} reference.
     */
    static final Class<AgentConfigurationStorage> CONFIG_STORAGE = AgentConfigurationStorage.class;


}
