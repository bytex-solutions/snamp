package com.itworks.snamp.connectors;

import java.io.IOException;
import java.nio.*;
import java.util.*;

/**
 * Provide support for channels through management connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ChannelSupport {
    /**
     * Represents input channel listener.
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static interface ChannelListener{
        /**
         * Receives a chunk from the input channel.
         * @param buffer The chunk from the input channel.
         */
        void receive(final ByteBuffer buffer);

        /**
         * Handles I/O error.
         * @param e An error occurred in the channel during reading.
         * @param fatal {@literal true}, if channel is not recoverable and this listener
         *                             will never be called; otherwise, {@literal false}.
         */
        void handleError(final IOException e, final boolean fatal);
    }

    /**
     * Represents chunk of binary data to be written into channel.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface ChannelWriter{
        /**
         * Writes data into output buffer.
         * @param buffer Output buffer.
         * @return {@literal true}, if written chunk is final and underlying channel can flush stream;
         *          otherwise, {@literal false}.
         */
        boolean write(final ByteBuffer buffer) throws BufferOverflowException;

        /**
         * Notifies the chunk about flushing stream.
         * <p>
         *     It is highly recommended to write next chunk after receiving this notification.
         *     Otherwise, the channel will create the buffer for all input chunks and this action
         *     may causes buffer overflow. This situation may happens when the speed of channel write operation
         *     is greater that write speed to the underlying back system.
         * </p>
         * @param bufferSize Size of the flushed buffer.
         */
        void flush(final long bufferSize);

        /**
         *
         * @param e
         * @param fatal
         */
        void handleError(final IOException e, final boolean fatal);
    }

    /**
     * Connects to the specified input stream.
     * @param channelId Unique identifier of the channel to connect.
     * @param streamName The stream name.
     * @param options The stream connection options.
     * @return The stream descriptor.
     */
    ChannelMetadata connectChannel(final String channelId, final String streamName, final Map<String, String> options);

    /**
     * Obtains meta-information about connected channel.
     * @param channelId Unique identifier of the connected channel.
     * @return Meta-information about connected channel; or {@literal null}, if channel is not connected.
     */
    ChannelMetadata getChannelInfo(final String channelId);

    /**
     * Returns a read-only collection of registered channel IDs.
     * @return A read-only collection of registered channel IDs.
     */
    Collection<String> getConnectedChannels();

    /**
     * Disconnects channel.
     * @param channelId Unique identifier of the channel to disconnect.
     * @return {@literal true}, if channel is disconnected successfully; otherwise, {@literal false}.
     */
    boolean disconnectChannel(final String channelId);

    /**
     * Adds a new listener for the input channel.
     *
     * @param listenedId Unique identifier of the listener.
     * @param channelId Unique identifier of the channel to listen.
     * @param listener The input channel listener to subscribe.
     * @return {@literal true}, if listener is added successfully; otherwise, {@literal false}.
     */
    boolean addChannelListener(final String listenedId, final String channelId, final ChannelListener listener);

    /**
     * Removes the listener from the input channel.
     * @param listenerId Unique identifier of the listener.
     * @return {@literal true}, if the listener removed successfully from the channel; otherwise, {@literal false}.
     */
    boolean removeChannelListener(final String listenerId);

    /**
     * Captures the stream for write operation.
     * @param callerId Unique identifier that is known only from caller code.
     * @param channelId An identifier of the connected input channel.
     * @return {@literal true}, if channel is captured successfully (it is connected and available for output);
     *      otherwise, {@literal false}.
     */
    boolean captureStream(final String callerId, final String channelId);

    /**
     * Writes into the specified output channel.
     * @param callerId An identifier of the caller ID.
     * @param writer A chunk of data to write.
     * @return {@literal true}, if chunk is written successfully; otherwise, {@literal false}.
     */
    boolean writeToChannel(final String callerId, final ChannelWriter writer);

    /**
     * Releases stream and makes it available for writing from other callers.
     * @param callerId An identifier of the caller.
     * @return {@literal true}, if stream is released successfully; otherwise, {@literal false}.
     */
    boolean releaseStream(final String callerId);
}