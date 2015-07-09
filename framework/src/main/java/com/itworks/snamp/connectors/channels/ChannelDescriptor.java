package com.itworks.snamp.connectors.channels;

import javax.management.ImmutableDescriptor;
import java.util.Map;

/**
 * Represents descriptor of the channel.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class ChannelDescriptor extends ImmutableDescriptor {
    private static final long serialVersionUID = -516459089021572254L;

    private ChannelDescriptor(final Map<String, ?> fields){
        super(fields);
    }
}
