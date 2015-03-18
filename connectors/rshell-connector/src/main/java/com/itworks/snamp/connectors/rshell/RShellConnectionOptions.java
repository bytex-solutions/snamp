package com.itworks.snamp.connectors.rshell;

import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.jcommands.channels.CommandExecutionChannels;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

/**
 * Represents connection options for the RShell connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellConnectionOptions {
    private final String connectionString;
    private final Map<String, String> connectionParams;

    RShellConnectionOptions(final String connectionString, final Map<String, String> params){
        this.connectionParams = params;
        this.connectionString = connectionString;
    }

    public boolean equals(final String connectionString,
                          final Map<String, String> connectionParams){
        return Objects.equals(this.connectionString, connectionString) &&
                Objects.equals(connectionParams, this.connectionParams);
    }

    /**
     * Creates a new instance of the execution channel.
     * @return A new instance of the execution channel.
     * @throws Exception Unable to instantiate the channel.
     */
    CommandExecutionChannel createExecutionChannel() throws Exception {
        try {
            final URI u = new URI(connectionString);
            return CommandExecutionChannels.createChannel(u, connectionParams);
        } catch (final URISyntaxException e) {
            return CommandExecutionChannels.createChannel(connectionString, connectionParams);
        }
    }
}
