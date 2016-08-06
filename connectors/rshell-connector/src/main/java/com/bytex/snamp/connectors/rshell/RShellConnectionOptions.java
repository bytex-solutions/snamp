package com.bytex.snamp.connectors.rshell;

import com.google.common.collect.ImmutableMap;
import com.bytex.jcommands.CommandExecutionChannel;
import com.bytex.jcommands.channels.CommandExecutionChannels;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Represents connection options for the RShell connector.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class RShellConnectionOptions implements Callable<CommandExecutionChannel> {
    private final String connectionString;
    private final ImmutableMap<String, String> connectionParams;

    RShellConnectionOptions(final String connectionString, final Map<String, String> params){
        this.connectionParams = ImmutableMap.copyOf(params);
        this.connectionString = connectionString;
    }

    /**
     * Creates a new instance of the execution channel.
     * @return A new instance of the execution channel.
     * @throws Exception Unable to instantiate the channel.
     */
    CommandExecutionChannel createExecutionChannel() throws Exception {
        return call();
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public CommandExecutionChannel call() throws Exception {
        try {
            final URI parsedUrl = new URI(connectionString);
            return parsedUrl.isAbsolute() ?
                    CommandExecutionChannels.createChannel(parsedUrl, connectionParams) :
                    CommandExecutionChannels.createChannel(parsedUrl.getPath(), connectionParams);
        } catch (final URISyntaxException e) {
            return CommandExecutionChannels.createChannel(connectionString, connectionParams);
        }
    }

    @Override
    public String toString() {
        return connectionString;
    }
}
