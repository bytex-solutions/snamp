package com.bytex.snamp.connectors.channels;

import javax.management.MBeanFeatureInfo;
import java.nio.channels.AsynchronousChannel;
import java.util.Objects;

/**
 * Represents information about channels provided by the managed resource connector.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class MBeanChannelInfo extends MBeanFeatureInfo implements ChannelDescriptorRead {
    private final ChannelDescriptor descriptor;
    private final boolean readable;
    private final boolean writable;

    public MBeanChannelInfo(final String name,
                            final String description,
                            final Class<? extends AsynchronousChannel> channelType,
                            final boolean readable,
                            final boolean writable,
                            final ChannelDescriptor descriptor) {
        super(name, description, descriptor);
        this.descriptor = Objects.requireNonNull(descriptor);
        this.readable = readable;
        this.writable = writable;
    }

    public final boolean isReadable(){
        return readable;
    }

    public final boolean isWritable(){
        return writable;
    }

    @Override
    public final ChannelDescriptor getDescriptor() {
        return descriptor;
    }
}
