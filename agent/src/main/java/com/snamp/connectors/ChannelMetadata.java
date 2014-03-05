package com.snamp.connectors;

/**
 * Represents description of the channel.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ChannelMetadata extends ManagementEntityMetadata {
    /**
     * Represents channel type.
     */
    public static enum ChannelType{
        /**
         * Input channel that is used to receive binary content.
         */
        INPUT,

        /**
         * Output channel that is used to send binary content.
         */
        OUTPUT
    }

    /**
     * Gets name of this input stream.
     * @return The name of this input stream.
     */
    public String getName();

    /**
     * Gets type of this channel.
     * @return The channel type.
     */
    public ChannelType getType();

    /**
     * Gets portion of binary data that can be written into the channel or can
     * be obtained from the channel at time.
     * @return Size of chunk, in bytes.
     */
    public long getChunkSize();
}
