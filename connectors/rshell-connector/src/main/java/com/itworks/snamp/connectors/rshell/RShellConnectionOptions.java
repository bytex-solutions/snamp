package com.itworks.snamp.connectors.rshell;

import com.google.common.collect.ImmutableMap;
import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.jcommands.channels.CommandExecutionChannels;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;

import java.math.BigInteger;
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
    private final ImmutableMap<String, String> connectionParams;

    RShellConnectionOptions(final String connectionString, final Map<String, String> params){
        this.connectionParams = ImmutableMap.copyOf(params);
        this.connectionString = connectionString;
    }

    static BigInteger computeConfigurationHash(final String connectionString,
                                               final Map<String, String> options){
        return AbstractManagedResourceConnector.computeConnectionParamsHashCode(connectionString, options);
    }

    BigInteger getConfigurationHash(){
        return computeConfigurationHash(connectionString, connectionParams);
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
