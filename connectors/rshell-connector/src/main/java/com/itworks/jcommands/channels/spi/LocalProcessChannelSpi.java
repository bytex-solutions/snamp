package com.itworks.jcommands.channels.spi;

import com.itworks.jcommands.channels.LocalProcessExecutionChannel;

import java.util.Map;

/**
 * Represents factory of {@link com.itworks.jcommands.channels.LocalProcessExecutionChannel}.
 * This class cannot be inherited.
 */
public final class LocalProcessChannelSpi implements CommandExecutionChannelSpi {
    @Override
    public LocalProcessExecutionChannel create(final Map<String, String> params) throws Exception {
        return new LocalProcessExecutionChannel(params);
    }

    @Override
    public String getType() {
        return LocalProcessExecutionChannel.CHANNEL_NAME;
    }

    @Override
    public String toString() {
        return getType();
    }
}
