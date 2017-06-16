package com.bytex.snamp.io;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.Stateful;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Represents fast implementation of {@link java.io.ByteArrayInputStream}.
 * @author Roman Sakno
 */
final class ByteBufferInputStream extends InputStream implements SafeCloseable, Stateful {
    private final ByteBuffer buf;

    ByteBufferInputStream(final ByteBuffer buffer) {
        (buf = buffer.asReadOnlyBuffer()).position(0).limit(buffer.position());
    }

    ByteBufferInputStream(final byte[] buffer){
        buf = Buffers.wrap(buffer);
    }

    @Override
    public int available() {
        return buf.remaining();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(final int readlimit) {
        buf.mark();
    }

    @Override
    public void reset() {
        buf.reset();
    }

    @Override
    public int read() {
        return buf.hasRemaining() ? buf.get() : -1;
    }

    @Override
    public int read(@Nonnull final byte[] b, int off, int len) {
        if (buf.hasRemaining()) {
            if (len > buf.remaining())
                len = buf.remaining();
            buf.get(b, off, len);
            return len;
        } else
            return -1;
    }

    @Override
    public long skip(long n) {
        if (n > 0 && buf.hasRemaining()) {
            if (n > buf.remaining())
                n = buf.remaining();
            buf.position((int) n + buf.position());
            return n;
        } else
            return 0;
    }

    @Override
    public void close() {
        buf.clear();
    }
}
