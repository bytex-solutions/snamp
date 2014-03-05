package com.snamp.connectors.util;

import com.snamp.connectors.ChannelSupport;
import com.snamp.internal.MethodStub;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.*;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static com.snamp.connectors.ChannelSupport.ChannelListener;

/**
 * Represents pipelined implementation of {@link ChannelListener} interface that can be used
 * to transform data from the channel into the Java stream.
 * @param <TOutput> Type of the channel data receiver.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ChannelPipeline<TOutput> implements ChannelListener {

    private final List<IOException> errors;
    private final int capacity;
    private final TOutput outputStream;

    /**
     * Creates a new pipeline that redirects channel data to the specified writable stream.
     * @param output Channel data receiver. Cannot be {@literal null}.
     * @param errorBufferCapacity List capacity of received I/O errors.
     * @return A new instance of the pipeline.
     */
    public static ChannelPipeline<WritableByteChannel> create(final WritableByteChannel output, final int errorBufferCapacity){
        return new ChannelPipeline<WritableByteChannel>(output, errorBufferCapacity) {
            @Override
            protected final void redirect(final ByteBuffer source, final WritableByteChannel destination) throws IOException {
                destination.write(source);
            }
        };
    }

    /**
     * Creates a new pipeline that redirects channel data to the specified output stream.
     * @param output Channel data receiver. Cannot be {@literal null}.
     * @param errorBufferCapacity List capacity of received I/O errors.
     * @return A new instance of the pipeline.
     */
    public static ChannelPipeline<OutputStream> create(final OutputStream output, final int errorBufferCapacity){
        return new ChannelPipeline<OutputStream>(output, errorBufferCapacity) {
            @Override
            protected final void redirect(final ByteBuffer source, final OutputStream destination) throws IOException {
                while (source.hasRemaining())
                    destination.write(source.get());
            }
        };
    }

    /**
     * Initializes a new pipeline.
     * @param output Channel data receiver. Cannot be {@literal null}.
     * @param errorBufferCapacity List capacity of received I/O errors.
     */
    protected ChannelPipeline(final TOutput output, final int errorBufferCapacity){
        if(output == null) throw new IllegalArgumentException("output is null.");
        outputStream = output;
        errors = new ArrayList<>(capacity = errorBufferCapacity);
    }

    /**
     * Returns unique identifier of this pipeline used for registration at the channel.
     * @return Unique identifier of this pipeline used for registration at the channel.
     */
    public final String getListenerId(){
        return Integer.toString(hashCode());
    }

    /**
     * Attaches this listener to the specified channel.
     * @param channel The channel support object. Cannot be {@literal null}.
     * @param channelId The identifier of the connected channel.
     * @return {@literal true}, if this pipeline is created successfully; otherwise, {@literal false}.
     * @see ChannelSupport#addChannelListener(String, String, com.snamp.connectors.ChannelSupport.ChannelListener)
     */
    public final boolean attachTo(final ChannelSupport channel, final String channelId){
        return channel.addChannelListener(getListenerId(), channelId, this);
    }

    /**
     * Detaches this pipeline from the channel.
     * @param channel The channel support object. Cannot be {@literal null}.
     * @return {@literal true} if this pipeline is detached successfully; otherwise, {@literal false}.
     * @see ChannelSupport#removeChannelListener(String)
     */
    public final boolean detachFrom(final ChannelSupport channel){
        return channel.removeChannelListener(getListenerId());
    }

    /**
     * Writes received chunk of data from the channel into the specified destination.
     * @param source The received chunk of data.
     * @param destination The output stream.
     * @throws IOException Some I/O exception occurred at the destination stream.
     */
    protected abstract void redirect(final ByteBuffer source, final TOutput destination) throws IOException;

    /**
     * Receives a chunk from the input channel.
     *
     * @param buffer The chunk from the input channel.
     */
    @Override
    public final void receive(final ByteBuffer buffer) {
        try{
            redirect(buffer, outputStream);
        }
        catch (final IOException e){
            handleError(e, false);
        }
    }

    /**
     * Returns read-only list of I/O errors.
     * @return Read-only list of I/O errors.
     */
    public synchronized final List<IOException> getErrors(){
        return new ArrayList<>(errors);
    }

    /**
     * Handles I/O error.
     *
     * <p>
     *     It is recommended to call base implementation of this method
     *     when overriding in the derived class.
     * </p>
     * @param e     An error occurred in the channel during reading.
     * @param fatal {@literal true}, if channel is not recoverable and this listener
     *              will never be called; otherwise, {@literal false}.
     */
    @Override
    public synchronized void handleError(final IOException e, final boolean fatal){
        if(errors.size() == capacity)
            errors.clear();
        errors.add(e);
    }
}
