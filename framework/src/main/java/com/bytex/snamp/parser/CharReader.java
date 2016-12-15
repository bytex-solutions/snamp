package com.bytex.snamp.parser;

import com.bytex.snamp.SafeCloseable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Objects;

/**
 * Represents position of token in the stream.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@NotThreadSafe
public final class CharReader extends Reader implements SafeCloseable {
    private CharSequence sequence;
    private int position;

    CharReader(final CharSequence sequence){
        this.sequence = Objects.requireNonNull(sequence);
        position = 0;
    }

    public CharReader(final String str) {
        this((CharSequence) str);
    }

    public CharReader(final CharBuffer buffer) {
        this((CharSequence) buffer);
    }

    public CharReader(final char[] chars){
        this(CharBuffer.wrap(chars));
    }

    private void ensureOpen() throws IOException {
        if (sequence == null) throw new IOException("Stream closed");
    }

    private boolean hasMore(){
        return position < sequence.length();
    }

    public synchronized int getLength() throws IOException{
        ensureOpen();
        return sequence.length();
    }

    public synchronized int getPosition() {
        return position;
    }

    public synchronized int getRemaining() throws IOException{
        return Math.max(0, getLength() - getPosition());
    }

    @Override
    public boolean ready() throws IOException {
        ensureOpen();
        return true;
    }

    public synchronized CharSequence readTo(final char stopChar) throws IOException {
        ensureOpen();
        if(hasMore()) {
            final int start = position;
            while (hasMore()){
                final char ch = sequence.charAt(position);
                if(stopChar == ch)
                    break;
                else
                    position += 1;
            }
            return sequence.subSequence(start, position);
        }
        else
            return "";
    }

    /**
     * Read all characters from the current position to the end of underlying stream.
     * @return All characters from the current position to the end of underlying stream.
     * @throws IOException Stream is closed.
     */
    public synchronized CharSequence readToEnd() throws IOException {
        ensureOpen();
        final CharSequence result = sequence.subSequence(position, sequence.length());
        position = sequence.length();
        return result;
    }

    /**
     * Skip single character.
     * @return {@literal true}, if character is skipped successfully; otherwise, {@link false}.
     * @throws IOException Reader is closed.
     */
    public synchronized boolean skip() throws IOException{
        ensureOpen();
        position += 1;
        return position <= sequence.length();
    }

    @Override
    public synchronized long skip(final long n) throws IOException {
        ensureOpen();
        if(!hasMore())
            return 0L;
        // Bound skip by beginning and end of the source
        final long skipped = Math.min(getRemaining(), n);
        position += skipped;
        return skipped;
    }

    /**
     * Gets character at the current position in the stream.
     * @return Character at the current position in the stream.
     * @throws IOException Reader is closed or end-of-stream is reached.
     */
    public synchronized char get() throws IOException {
        ensureOpen();
        if(position >= sequence.length())
            throw new IOException("End of stream");
        return sequence.charAt(position);
    }

    @Override
    public synchronized int read() throws IOException {
        ensureOpen();
        return hasMore() ? sequence.charAt(position++) : -1;
    }

    @Override
    public synchronized int read(@Nonnull final char[] cbuf) throws IOException {
        ensureOpen();
        int index;
        for (index = 0; hasMore() && index < cbuf.length; index++, position++)
            cbuf[index] = sequence.charAt(index);
        return index;
    }

    @Override
    public synchronized int read(@Nonnull final char[] cbuf, int off, final int len) throws IOException {
        ensureOpen();
        int index;
        for(index = 0; hasMore() && off < len; index++, position++)
            cbuf[off] = sequence.charAt(index);
        return index;
    }

    @Override
    public synchronized void reset() throws IOException {
        ensureOpen();
        position = 0;
    }

    @Override
    public synchronized String toString() {
        return sequence == null ? "<STREAM IS CLOSED>" : String.format("Source: [%s]. Position: [%s]", sequence, position);
    }

    @Override
    public int read(@Nonnull final CharBuffer target) throws IOException {
        ensureOpen();
        int remaining = target.remaining();
        int count = 0;
        while (hasMore() && remaining > 0) {
            target.append(sequence.charAt(position++));
            remaining -= 1;
            count += 1;
        }
        return count;
    }

    @Override
    public synchronized void close() {
        sequence = null;
    }
}
