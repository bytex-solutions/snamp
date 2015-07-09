package com.itworks.snamp.connectors.channels;

import javax.management.Descriptor;
import javax.management.DescriptorRead;

/**
 * Provides access to channel descriptor.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface ChannelDescriptorRead extends DescriptorRead {
    /**
     * Gets descriptor of the channel.
     * @return Descriptor of the channel.
     */
    @Override
    ChannelDescriptor getDescriptor();
}
