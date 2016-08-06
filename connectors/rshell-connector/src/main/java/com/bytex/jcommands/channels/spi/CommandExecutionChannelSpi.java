package com.bytex.jcommands.channels.spi;

import com.bytex.jcommands.CommandExecutionChannel;

import java.util.Map;

/**
 * Represents producer of {@link com.bytex.jcommands.CommandExecutionChannel} implementation.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface CommandExecutionChannelSpi {
    /**
     * Creates a new instance of the execution channel.
     * @param params Channel initialization parameters. Cannot be {@literal null}.
     * @return A new instance of the execution channel.
     * @throws Exception Unable to instantiate channel.
     */
    CommandExecutionChannel create(final Map<String, String> params) throws Exception;

    /**
     * Gets type of this channel.
     * @return The type of this channel.
     */
    String getType();
}
