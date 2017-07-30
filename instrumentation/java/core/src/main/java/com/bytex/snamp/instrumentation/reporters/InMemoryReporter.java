package com.bytex.snamp.instrumentation.reporters;

import com.bytex.snamp.instrumentation.measurements.Measurement;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents in-memory reporter of measurements that can be used for testing purposes.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class InMemoryReporter extends LinkedBlockingQueue<Measurement> implements Reporter {
    private static final long serialVersionUID = -2569092943568592831L;
    private volatile boolean closed = false;

    private void ensureOpen() throws IOException{
        if(closed)
            throw new IOException("In-memory reporter is closed");
    }

    @Override
    public boolean isConnected() {
        return !closed;
    }

    /**
     * Determines whether this sender is asynchronous.
     *
     * @return Always {@literal false}.
     */
    @Override
    public boolean isAsynchronous() {
        return false;
    }

    /**
     * Nothing to do.
     * @throws IOException The reporter is closed.
     */
    @Override
    public void flush() throws IOException {
        ensureOpen();
    }

    /**
     * Saves measurement into queue.
     *
     * @param measurements A set of measurements to enqueue.
     * @throws IOException The reporter is closed.
     */
    @Override
    public void report(final Measurement... measurements) throws IOException {
        ensureOpen();
        addAll(Arrays.asList(measurements));
    }

    /**
     * Removes all measurements from this queue.
     */
    @Override
    public void close() {
        closed = true;
        clear();
    }
}
