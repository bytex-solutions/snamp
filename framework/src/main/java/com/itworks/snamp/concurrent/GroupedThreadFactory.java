package com.itworks.snamp.concurrent;

import java.util.concurrent.ThreadFactory;

/**
 * Represents thread factory which spawns a new threads in the same group.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class GroupedThreadFactory extends ThreadGroup implements ThreadFactory {
    /**
     * Represents a priority for all newly created threads.
     */
    protected final int newThreadPriority;

    /**
     * Initializes a new thread factory.
     * @param groupName The name of the thread group.
     * @param newThreadPriority The priority for all newly created threads.
     */
    public GroupedThreadFactory(final String groupName, final int newThreadPriority) {
        super(groupName);
        this.newThreadPriority = newThreadPriority;
    }

    /**
     * Constructs a new {@code Thread}.  Implementations may also initialize
     * priority, name, daemon status, {@code ThreadGroup}, etc.
     *
     * @param r a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     * create a thread is rejected
     */
    @Override
    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(this, r);
        t.setDaemon(true);
        t.setPriority(newThreadPriority);
        return t;
    }
}
