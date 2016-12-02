package com.bytex.snamp.instrumentation.reporting;

import com.bytex.snamp.instrumentation.Measurement;
import com.sun.xml.internal.ws.Closeable;

import java.io.IOException;

/**
 * Sends measurements to SNAMP server.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
public interface Reporter extends Closeable {
    /**
     * Determines whether this sender is asynchronous.
     * @return {@literal true} if this sender is asynchronous; otherwise, {@literal false}.
     */
    boolean isAsynchronous();

    /**
     * Flushes buffered measurements.
     */
    void flush();

    /**
     * Send one or more measurements.
     * @param measurements A set of measurements to send.
     * @throws IOException Some I/O error occurred when posting measurements to SNAMP.
     */
    void report(final Measurement... measurements) throws IOException;
}
