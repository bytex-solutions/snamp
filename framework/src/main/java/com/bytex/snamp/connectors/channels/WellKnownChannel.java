package com.bytex.snamp.connectors.channels;

import com.google.common.util.concurrent.Futures;

import javax.management.openmbean.OpenType;
import java.io.IOException;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NonWritableChannelException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Represents well-known channels that can be correctly interpreted by adapters and connectors.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class WellKnownChannel {
    /**
     * Represents channel with row bytes. This type of channel is very useful for infinite streams of data such as video streams.
     */
    public static final Class<AsynchronousByteChannel> BYTE_CHANNEL = AsynchronousByteChannel.class;

    /**
     * Represents typed channel in which elements are JMX open types. This type of channel is useful for capturing objects in the information systems, such as business messages in Messaging Middleware.
     */
    public static final Class<DataChannel> DATA_CHANNEL = DataChannel.class;

    /**
     * Converts iterator to the read-only channel.
     * @param iterator An iterator to be converted. Cannot be {@literal null}.
     * @param elementType Type of the elements in the sequence.
     * @param <D> Type of the elements in the sequence.
     * @return Read-only data channel.
     */
    public static <D> ReadOnlyDataChannel<D> toChannel(final Iterator<D> iterator,
                                                                    final OpenType<D> elementType){
       return new ReadOnlyDataChannel<D>() {
           @Override
           public OpenType<D> getDataType() {
               return elementType;
           }

           @Override
           public synchronized Future<D> read() {
               try{
                    return Futures.immediateFuture(iterator.next());
               }
               catch (final NoSuchElementException e){
                   return Futures.immediateFailedCheckedFuture(e);
               }
           }

           @Override
           public synchronized <A> void read(final A attachment, final CompletionHandler<D, ? super A> handler) {
               try {
                   handler.completed(iterator.next(), attachment);
               } catch (final NoSuchElementException e) {
                   handler.failed(e, attachment);
               }
           }

           @Override
           public void close() {

           }

           @Override
           public boolean isOpen() {
               return true;
           }
       };
    }

    /**
     * Converts collection to the read-only channel.
     * @param collection An iterator to be converted. Cannot be {@literal null}.
     * @param elementType Type of the elements in the sequence.
     * @param <D> Type of the elements in the sequence.
     * @return Read-only data channel.
     */
    public static <D> ReadOnlyDataChannel<D> toChannel(final Iterable<D> collection,
                                                                    final OpenType<D> elementType){
        return toChannel(collection.iterator(), elementType);
    }



    public static <D> DataChannel<D> toChannel(final List<D> list,
                                               final OpenType<D> elementType){
        return new DataChannel<D>() {
            private int position = 0;

            @Override
            public OpenType<D> getDataType() {
                return elementType;
            }

            @Override
            public synchronized Future<D> read() {
                if (position >= list.size())
                    return Futures.immediateFailedFuture(new NoSuchElementException());
                else try {
                    return Futures.immediateFuture(list.get(position++));
                } catch (final IndexOutOfBoundsException e) {
                    return Futures.immediateFailedFuture(e);
                }
            }

            @Override
            public synchronized <A> void read(final A attachment, final CompletionHandler<D, ? super A> handler) {
                if (position >= list.size())
                    handler.failed(new NoSuchElementException(), attachment);
                else
                    handler.completed(list.get(position++), attachment);
            }

            @Override
            public synchronized Future<Boolean> write(final D item) {
                try {
                    list.add(item);
                }
                catch (final IllegalArgumentException e){
                    return Futures.immediateFuture(false);
                }
                catch (final UnsupportedOperationException e){
                    return Futures.immediateFailedCheckedFuture(new NonWritableChannelException());
                }
                catch (final Exception e) {
                    return Futures.immediateFailedFuture(e);
                }
                return Futures.immediateFuture(true);
            }

            @Override
            public synchronized <A> void write(final D item, final A attachment, final CompletionHandler<Boolean, ? super A> handler) {
                try {
                    list.add(item);
                }
                catch (final IllegalArgumentException e){
                    handler.completed(false, attachment);
                }
                catch (final UnsupportedOperationException e){
                    handler.failed(new NonWritableChannelException(), attachment);
                }
                catch (final Exception e) {
                    handler.failed(e, attachment);
                }
                handler.completed(true, attachment);
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public boolean isOpen() {
                return true;
            }
        };
    }

    /**
     * Concatenates read-only and write-only channels into single bidirectional channel.
     * @param reader Read-only channel. Cannot be {@literal null}.
     * @param writer Write-only channel. Cannot be {@literal null}.
     * @param <D> Type of the elements in both channels.
     * @return Bidirectional data channel.
     */
    public static <D> DataChannel<D> union(final ReadOnlyDataChannel<D> reader, final WriteOnlyDataChannel<D> writer){
        if(!Objects.equals(reader.getDataType(), writer.getDataType()))
            throw new IllegalArgumentException(String.format("Incompatible element types: '%s' and '%s'", reader.getDataType(), writer.getDataType()));
        return new DataChannel<D>() {
            @Override
            public OpenType<D> getDataType() {
                return reader.getDataType();
            }

            @Override
            public Future<D> read() {
                return reader.read();
            }

            @Override
            public <A> void read(final A attachment, final CompletionHandler<D, ? super A> handler) {
                reader.read(attachment, handler);
            }

            @Override
            public Future<Boolean> write(final D item) {
                return writer.write(item);
            }

            @Override
            public <A> void write(final D item, final A attachment, final CompletionHandler<Boolean, ? super A> handler) {
                writer.write(item, attachment, handler);
            }

            @Override
            public void close() throws IOException {
                try {
                    reader.close();
                } finally {
                    writer.close();
                }
            }

            @Override
            public boolean isOpen() {
                return reader.isOpen() && writer.isOpen();
            }
        };
    }
}
