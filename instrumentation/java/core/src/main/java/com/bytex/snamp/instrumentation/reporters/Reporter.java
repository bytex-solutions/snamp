package com.bytex.snamp.instrumentation.reporters;

import com.bytex.snamp.instrumentation.measurements.Measurement;

import java.io.Closeable;
import java.io.IOException;

/**
 * Sends measurements to SNAMP server.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
public interface Reporter extends Closeable {
    /**
     * Represents OSGi service registration property indicating type of reporter.
     */
    String TYPE_PROPERTY = "reporter.type";

    /**
     * Determines whether this sender is asynchronous.
     * @return {@literal true} if this sender is asynchronous; otherwise, {@literal false}.
     */
    boolean isAsynchronous();

    /**
     * Determines whether this reporter is connected to the SNAMP server.
     * @return {@literal true}, if this reporter is connected to the server; otherwise, {@literal false}.
     */
    boolean isConnected();

    /**
     * Flushes buffered measurements.
     * @throws IOException Some I/O error occurred when posting measurements to SNAMP.
     */
    void flush() throws IOException;

    /**
     * Send one or more measurements.
     * @param measurements A set of measurements to send.
     * @throws IOException Some I/O error occurred when posting measurements to SNAMP.
     */
    void report(final Measurement... measurements) throws IOException;
}
