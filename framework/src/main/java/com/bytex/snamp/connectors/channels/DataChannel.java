package com.bytex.snamp.connectors.channels;

import javax.management.openmbean.OpenType;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;

/**
 * Represents generic data channel.
 * @param <D> Type of the data packets in the channel.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface DataChannel<D> extends AsynchronousChannel {
    /**
     * Gets type of elements in the channel.
     * @return The type of the elements in the channel.
     */
    OpenType<D> getDataType();

    Future<D> read();

    <A> void read(final A attachment, final CompletionHandler<D, ? super A> handler);

    Future<Boolean> write(final D item);

    <A> void write(final D item, final A attachment, final CompletionHandler<Boolean, ? super A> handler);
}
