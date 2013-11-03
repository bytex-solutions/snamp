package com.snamp.hosting.management;

import com.snamp.*;
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
     * Starts the manager synchronously.
     * @param context SNAMP hosting context.
     */
    @ThreadSafety(MethodThreadSafety.LOOP)
    public void start(final HostingContext context);

    /**
     * Stops the agent.
     * @return {@literal true} if manager is stopped successfully; otherwise, {@literal false}.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public boolean stop();
}
