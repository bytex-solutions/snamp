package com.itworks.snamp.connectors.channels;

import com.google.common.util.concurrent.Futures;

import java.io.Serializable;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NonWritableChannelException;
import java.util.concurrent.Future;

/**
 * Represents read-onyl data channel.
 * @param <D> Type of the data packets in the channel.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class ReadOnlyDataChannel<D> implements DataChannel<D> {
    @Override
    public final Future<Boolean> write(final D item){
        return Futures.immediateFailedFuture(new NonWritableChannelException());
    }

    @Override
    public final <A> void write(final D item, final A attachment, final CompletionHandler<Boolean, ? super A> handler) {
        handler.failed(new NonWritableChannelException(), attachment);
    }
}
