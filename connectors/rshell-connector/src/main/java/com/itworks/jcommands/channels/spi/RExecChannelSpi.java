package com.itworks.jcommands.channels.spi;

import com.itworks.jcommands.channels.RExecExecutionChannel;

import java.net.URI;
import java.util.Map;

/**
 * Represents factory for {@link com.itworks.jcommands.channels.RExecExecutionChannel} class.
 * This class cannot be inherited.
 */
public final class RExecChannelSpi implements URICommandExecutionChannelSpi {
    @Override
    public RExecExecutionChannel create(final URI connectionString, final Map<String, String> params) throws Exception {
        return new RExecExecutionChannel(connectionString, params);
    }

    @Override
    public RExecExecutionChannel create(final Map<String, String> params) throws Exception {
        return new RExecExecutionChannel(params);
    }

    @Override
    public String getType() {
        return RExecExecutionChannel.CHANNEL_NAME;
    }

    @Override
    public String toString() {
        return getType();
    }
}
