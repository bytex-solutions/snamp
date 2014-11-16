package com.itworks.snamp.connectors.rshell;

import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.jcommands.channels.CommandExecutionChannels;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Represents connection options for the RShell connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellConnectionOptions {
    private final String connectionString;
    private final Map<String, String> connectionParams;

    public RShellConnectionOptions(final String connectionString, final Map<String, String> params){
        this.connectionParams = params;
        this.connectionString = connectionString;
    }

    /**
     * Creates a new instance of the execution channel.
     * @return A new instance of the execution channel.
     * @throws Exception Unable to instantiate the channel.
     */
    public CommandExecutionChannel createExecutionChannel() throws Exception {
        try {
            final URI u = new URI(connectionString);
            return CommandExecutionChannels.createChannel(u, connectionParams);
        } catch (final URISyntaxException e) {
            return CommandExecutionChannels.createChannel(connectionString, connectionParams);
        }
    }
}
