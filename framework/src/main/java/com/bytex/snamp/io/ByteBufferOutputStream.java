package com.bytex.snamp.io;

import com.bytex.snamp.ArrayUtils;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Fast implementation of {@link OutputStream} based on direct buffer to save memory pressure and
 * improve performance of serialization routines.
 * @author Roman Sakno
 */
final class ByteBufferOutputStream extends OutputStream {

    private ByteBuffer buf;
    private final float resizeFactor;

    ByteBufferOutputStream(final int initialSize, final float resizeFactor){
        buf = newBuffer(initialSize);
        this.resizeFactor = resizeFactor;
    }

    /**
     * Constructs a stream with the given initial size
     */
    ByteBufferOutputStream(final int initialSize) {
        this(initialSize, 1.5F);
    }

    private static ByteBuffer newBuffer(final int size){
        return Buffers.allocByteBuffer(size, true);
    }

    /**
     * Ensures that we have a large enough buffer for the given size.
     */
    private void verifyBufferSize(int sz) {
        if (sz > buf.remaining()) {
            final ByteBuffer old = buf;
            old.limit(old.position()).position(0);
            final int newCapacity = Math.max(sz + buf.capacity(), Math.round(buf.capacity() * resizeFactor));
            buf = newBuffer(newCapacity);
            buf.put(old);
        }
    }

    public int getSize() {
        return buf.position();
    }

    @Override
    public void write(@Nonnull final byte[] b) {
        verifyBufferSize(b.length);
        buf.put(b);
    }

    @Override
    public void write(@Nonnull final byte[] b, int off, int len) {
        verifyBufferSize(len);
        buf.put(b, off, len);
    }

    @Override
    public void write(final int b) {
        verifyBufferSize(1);
        buf.put((byte) b);
    }

    byte[] toByteArray(){
        if(buf.hasArray())
            return buf.array();
        else if(buf.position() == 0)
            return ArrayUtils.emptyByteArray();
        else {
            buf.position(0);
            return Buffers.readRemaining(buf);
        }
    }

    /**
     * Returns a ByteArrayInputStream for reading back the written data
     */
    InputStream getInputStream() {
        return new ByteBufferInputStream(buf);
    }
}
