package com.bytex.jcommands.channels.spi;

import com.bytex.jcommands.CommandExecutionChannel;

import java.net.URI;
import java.util.Map;

/**
 * Represents producer for the execution channel that
 * supports {@link java.net.URI} as one of the initialization parameters.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface URICommandExecutionChannelSpi extends CommandExecutionChannelSpi {
    /**
     * Creates a new instance of the execution channel.
     * @param connectionString The connection string used to initialize the channel. Cannot be {@literal null}.
     * @param params Additional channel parameters.
     * @return A new instance of the channel.
     * @throws Exception Unable to instantiate the channel.
     */
    CommandExecutionChannel create(final URI connectionString,
                                   final Map<String, String> params) throws Exception;
}
