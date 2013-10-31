package com.snamp.hosting.management;

import com.snamp.hosting.Agent;


/**
 * Represents SNAMP management plugin.
 * @author roman
 */
public interface AgentManager {
    /**
     * Starts the manager.
     * @param agent An instance of SNAMP agent.
     * @return {@literal true} if manager is started successfully; otherwise, {@literal false}.
     */
    public boolean start(final Agent agent);

    /**
     * Stops the agent.
     * @return {@literal true} if manager is stopped successfully; otherwise, {@literal false}.
     */
    public boolean stop();
}
