package com.bytex.snamp.instrumentation.reporters;

import com.bytex.snamp.instrumentation.measurements.Measurement;

import java.io.IOException;

/**
 * Represents reporter with lazy initialization.
 * @param <R> Type of reporter to be initialized lazily.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class LazyReporter<R extends Reporter> implements Reporter {
    private volatile R reporter;

    /**
     * Creates a new instance of reporter.
     * @return A new instance of reporter.
     */
    protected abstract R createReporter();

    private synchronized R getReporterSync(){
        if(reporter == null)
            reporter = createReporter();
        return reporter;
    }

    private R getReporter(){
        return reporter == null ? getReporterSync() : reporter;
    }

    /**
     * Determines whether this sender is asynchronous.
     *
     * @return {@literal true} if this sender is asynchronous; otherwise, {@literal false}.
     */
    @Override
    public final boolean isAsynchronous() {
        final Reporter reporter =  getReporter();
        return reporter != null && reporter.isAsynchronous();
    }

    /**
     * Determines whether this reporter is connected to the SNAMP server.
     *
     * @return {@literal true}, if this reporter is connected to the server; otherwise, {@literal false}.
     */
    @Override
    public final boolean isConnected() {
        final R reporter = getReporter();
        return reporter != null && reporter.isConnected();
    }

    /**
     * Flushes buffered measurements.
     *
     * @throws IOException Some I/O error occurred when posting measurements to SNAMP.
     */
    @Override
    public final void flush() throws IOException {
        final R reporter = getReporter();
        if(reporter != null)
            reporter.flush();
    }

    /**
     * Send one or more measurements.
     *
     * @param measurements A set of measurements to send.
     * @throws IOException Some I/O error occurred when posting measurements to SNAMP.
     */
    @Override
    public final void report(final Measurement... measurements) throws IOException {
        final R reporter = getReporter();
        if (reporter != null)
            reporter.report(measurements);
    }

    @Override
    public final synchronized void close() throws IOException {
        if(reporter != null)
            reporter.close();
        reporter = null;
    }
}
