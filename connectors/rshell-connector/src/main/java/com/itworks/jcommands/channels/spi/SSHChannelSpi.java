package com.itworks.jcommands.channels.spi;

import com.itworks.jcommands.channels.SSHExecutionChannel;

import java.net.URI;
import java.util.Map;

/**
 * Represents factory for {@link com.itworks.jcommands.channels.SSHExecutionChannel} class.
 * This class cannot be inherited.
 */
public class SSHChannelSpi implements URICommandExecutionChannelSpi {
    @Override
    public SSHExecutionChannel create(final URI connectionString, final Map<String, String> params) throws Exception {
        return new SSHExecutionChannel(connectionString, params);
    }

    @Override
    public SSHExecutionChannel create(final Map<String, String> params) throws Exception {
        return new SSHExecutionChannel(params);
    }

    @Override
    public String getType() {
        return SSHExecutionChannel.CHANNEL_NAME;
    }

    @Override
    public String toString() {
        return getType();
    }
}
