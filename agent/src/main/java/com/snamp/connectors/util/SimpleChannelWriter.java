package com.snamp.connectors.util;

import com.snamp.SynchronizationEvent;
import com.snamp.TimeSpan;
import com.snamp.internal.MethodStub;

import java.io.IOException;
import java.nio.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.snamp.connectors.ChannelSupport.ChannelWriter;

/**
 * Represents an abstract class for building channel writers based on some type of binary data suppliers,
 * such as byte array.
 * @param <TInput> Type of the channel data supplier.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see ChannelWriter
 */
public abstract class SimpleChannelWriter<TInput> implements ChannelWriter {
    private final TInput inputData;
    private final SynchronizationEvent<Long> flushSignal;
    private final List<IOException> errors;
    private final int capacity;

    /**
     * Creates a new channel writer that writes an array of bytes into the output channel.
     * @param inputData An array of bytes to write. Cannot be {@literal null}.
     * @param errorsBufferCapacity Errors list capacity.
     * @return A new instance of the channel writer.
     */
    public static SimpleChannelWriter<byte[]> create(final byte[] inputData, final int errorsBufferCapacity){
        return new SimpleChannelWriter<byte[]>(inputData.clone(), errorsBufferCapacity) {
            private int position = 0;

            @Override
            protected boolean redirect(final byte[] source, final ByteBuffer destination) throws IOException, BufferOverflowException {
                while (destination.hasRemaining() && position < source.length)
                    destination.put(source[position++]);
                return position == source.length - 1;
            }
        };
    }

    /**
     * Initializes a new simple channel writer.
     * @param input
     */
    protected SimpleChannelWriter(final TInput input, final int errorsBufferCapacity){
        if(input == null) throw new IllegalArgumentException("input is null.");
        inputData = input;
        flushSignal = new SynchronizationEvent<>();
        errors = new ArrayList<>(this.capacity = errorsBufferCapacity);
    }

    /**
     * Returns unique identifier of this writer.
     * @return Unique identifier of this writer.
     */
    public final String getCallerId(){
        return Integer.toString(hashCode());
    }

    /**
     * Reads binary data from the source and writes it to the channel.
     * @param source The binary data supplier. Cannot be {@literal null}.
     * @param destination Output channel buffer.
     * @return {@literal true}, if there is no available binary data in the source; otherwise, {@literal false}.
     * @throws IOException Some I/O error occurred in the source stream.
     * @throws BufferOverflowException Destination buffer too small to receive binary data.
     */
    protected abstract boolean redirect(final TInput source, final ByteBuffer destination) throws IOException, BufferOverflowException;

    /**
     * Writes data into output buffer.
     *
     * @param buffer Output buffer.
     * @return {@literal true}, if written chunk is final and underlying channel can flush stream;
     *         otherwise, {@literal false}.
     */
    @Override
    public final boolean write(final ByteBuffer buffer) throws BufferOverflowException {
        try {
            return redirect(inputData, buffer);
        }
        catch (final IOException e) {
            handleError(e, false);
            return true;
        }
    }

    /**
     * Notifies the chunk about flushing stream.
     * <p>
     * It is highly recommended to write next chunk after receiving this notification.
     * Otherwise, the channel will create the buffer for all input chunks and this action
     * may causes buffer overflow. This situation may happens when the speed of channel write operation
     * is greater that write speed to the underlying back system.
     * </p>
     * @param flushedBufferSize Flushed buffer size.
     */
    @Override
    public final void flush(final long flushedBufferSize) {
        flushSignal.fire(flushedBufferSize);
    }

    /**
     * Returns an awaitor that is used to synchronizes with flush signal.
     * @return An awaitor that is used to synchronizes with flush signal.
     */
    public final SynchronizationEvent.Awaitor<Long> getFlushAwaitor(){
        return flushSignal.getAwaitor();
    }

    /**
     * Blocks the caller thread until the underlying channel will not flush.
     * @param timeout Flush operation waiting time.
     * @throws TimeoutException Timeout is reached.
     * @throws InterruptedException The caller thread is interrupted.
     */
    public final void waitForFlush(final TimeSpan timeout) throws TimeoutException, InterruptedException {
        getFlushAwaitor().await(timeout);
    }

    /**
     * Returns a read-only collection of I/O errors.
     * @return
     */
    public final synchronized List<IOException> getErrors(){
        return new ArrayList<>(errors);
    }

    /**
     * Writes I/O error into the internal collection of errors.
     * <p>
     *     It is recommended to call base implementation of this method
     *     when overriding in the derived class.
     * </p>
     * @param e An error to save.
     * @param fatal
     */
    @Override
    public synchronized void handleError(final IOException e, final boolean fatal) {
        if(errors.size() == capacity)
            errors.clear();
        errors.add(e);
    }
}
