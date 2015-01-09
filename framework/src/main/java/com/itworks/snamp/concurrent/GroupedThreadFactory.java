package com.itworks.snamp.concurrent;

import java.util.concurrent.ThreadFactory;

/**
 * Represents thread factory which spawns a new threads in the same group.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class GroupedThreadFactory extends ThreadGroup implements ThreadFactory, AutoCloseable {
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
     * Initializes a new thread factory that uses {@link java.lang.Thread#NORM_PRIORITY}
     * as a priority for all created threads.
     * @param groupName The name of the thread group.
     */
    public GroupedThreadFactory(final String groupName){
        this(groupName, Thread.NORM_PRIORITY);
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
    public Thread newThread(@SuppressWarnings("NullableProblems") final Runnable r) {
        final Thread t = new Thread(this, r);
        t.setDaemon(true);
        t.setPriority(newThreadPriority);
        return t;
    }

    /**
     * Destroys this thread group.
     * @see #destroy()
     */
    @Override
    public final void close() {
        destroy();
    }
}