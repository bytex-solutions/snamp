package com.snamp.hosting.management;

import com.snamp.PlatformPlugin;
import com.snamp.hosting.HostingContext;


/**
 * Represents SNAMP management plugin.
 * @author roman
 */
public interface AgentManager extends PlatformPlugin, AutoCloseable {
    /**
     * Represents name of the system property that holds the name of the Agent manager.
     */
    static final String MANAGER_NAME = "com.snamp.manager";

    /**
     * Starts the manager.
     * @param context SNAMP hosting context.
     * @return {@literal true} if manager is started successfully; otherwise, {@literal false}.
     */
    public boolean start(final HostingContext context);

    /**
     * Stops the agent.
     * @return {@literal true} if manager is stopped successfully; otherwise, {@literal false}.
     */
    public boolean stop();

    /**
     * Blocks the current thread until the manager will not signals to continue.
     */
    public void waitForTermination();
}
