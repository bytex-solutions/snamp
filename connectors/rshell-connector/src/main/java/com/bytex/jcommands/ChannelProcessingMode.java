package com.bytex.jcommands;

/**
 * Represents command execution mode.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public enum ChannelProcessingMode {
    /**
     * Indicates that the channel creates connection for each execution session.
     */
    CONNECTION_PER_EXECUTION,

    /**
     * Indicates that the channel uses single connection for all execution sessions.
     */
    SINGLETON_CONNECTION
}
