package com.itworks.snamp.connectors.channels;

import com.google.common.util.concurrent.Futures;

import java.nio.channels.CompletionHandler;
import java.nio.channels.NonReadableChannelException;
import java.util.concurrent.Future;

/**
 * Represents write-only data channel.
 * @param <D> Type of the data packets in the channel.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class WriteOnlyDataChannel<D> implements DataChannel<D> {
    @Override
    public final Future<D> read() {
        return Futures.immediateFailedFuture(new NonReadableChannelException());
    }

    @Override
    public final  <A> void read(final A attachment, final CompletionHandler<D, ? super A> handler) {
        handler.failed(new NonReadableChannelException(), attachment);
    }
}
