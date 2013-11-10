package com.snamp.hosting;

import com.snamp.Aggregator;

/**
 * Represents agent hosting context and describes SNAMP environment at runtime.
 * <p>
 *  This is an infrastructure interface and you should not implement it directly in your code.
 *  Implementation of this interface provided by SNAMP platform.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface HostingContext extends Aggregator {
    /**
     * Represents {@link Agent} reference.
     */
    static final Class<Agent> AGENT = Agent.class;

    /**
     * Represents {@link AgentConfigurationStorage} reference.
     */
    static final Class<AgentConfigurationStorage> CONFIG_STORAGE = AgentConfigurationStorage.class;
}
