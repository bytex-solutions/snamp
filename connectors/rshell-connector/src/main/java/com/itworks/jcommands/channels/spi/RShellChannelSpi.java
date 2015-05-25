package com.itworks.jcommands.channels.spi;

import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.jcommands.channels.RShellExecutionChannel;

import java.net.URI;
import java.util.Map;

/**
 * Represents factory for {@link com.itworks.jcommands.channels.RShellExecutionChannel} class.
 * This class cannot be inherited.
 */
public final class RShellChannelSpi implements URICommandExecutionChannelSpi {
    @Override
    public RShellExecutionChannel create(final URI connectionString, final Map<String, String> params) throws Exception {
        return new RShellExecutionChannel(connectionString, params);
    }

    @Override
    public RShellExecutionChannel create(final Map<String, String> params) throws Exception {
        return new RShellExecutionChannel(params);
    }

    @Override
    public String getType() {
        return RShellExecutionChannel.CHANNEL_NAME;
    }

    @Override
    public String toString() {
        return getType();
    }
}
