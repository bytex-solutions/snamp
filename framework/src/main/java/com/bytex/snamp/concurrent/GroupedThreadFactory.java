package com.bytex.snamp.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

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
    private final AtomicLong threadNum;

    /**
     * Initializes a new thread factory.
     * @param groupName The name of the thread group.
     * @param newThreadPriority The priority for all newly created threads.
     */
    public GroupedThreadFactory(final String groupName, final int newThreadPriority) {
        super(groupName);
        this.newThreadPriority = newThreadPriority;
        this.threadNum = new AtomicLong(0L);
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
     * Gets total number of created threads inside of this group.
     * @return The total number of created threads inside of this group.
     */
    public final long createdCount(){
        return threadNum.get();
    }

    /**
     * Generates a new name for the thread.
     * @return A new unique name of the thread.
     */
    protected String generateThreadName(){
        return getName() + "#" + threadNum.getAndIncrement();
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
    public final Thread newThread(@SuppressWarnings("NullableProblems") final Runnable r) {
        final Thread t = new Thread(this, r, generateThreadName());
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
