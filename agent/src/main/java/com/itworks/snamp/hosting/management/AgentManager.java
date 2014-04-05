package com.itworks.snamp.hosting.management;

import com.itworks.snamp.core.PlatformPlugin;
import com.itworks.snamp.hosting.HostingContext;
import com.itworks.snamp.internal.MethodThreadSafety;
import com.itworks.snamp.internal.ThreadSafety;


/**
 * Represents an interface for SNAMP manager plug-in.
 * <p>
 *     Manager is SNAMP platform component that exposes management infrastructure
 *     to administrators or tools, such as Web console, or command-line string.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface AgentManager extends PlatformPlugin, AutoCloseable {
    /**
     * Represents name of the system property that holds the name of the Agent manager.
     */
    static final String MANAGER_NAME = "com.snamp.manager";

    /**
     * Starts the manager synchronously and blocks this thread until exit.
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
