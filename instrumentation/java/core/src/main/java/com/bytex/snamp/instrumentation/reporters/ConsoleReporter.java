package com.bytex.snamp.instrumentation.reporters;

import com.bytex.snamp.instrumentation.measurements.Measurement;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Represents reporter that writes all incoming measurements into STDOUT.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConsoleReporter implements Reporter {
    private final PrintStream output;

    /**
     * Initializes a new text reporter that writes all incoming measurements into STDOUT.
     */
    public ConsoleReporter(){
        output = System.out;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    /**
     * Determines whether this sender is asynchronous.
     *
     * @return {@literal true} if this sender is asynchronous; otherwise, {@literal false}.
     */
    @Override
    public boolean isAsynchronous() {
        return false;
    }

    /**
     * Flushes buffered measurements.
     */
    @Override
    public void flush() {
        output.flush();
    }

    /**
     * Send one or more measurements.
     *
     * @param measurements A set of measurements to send.
     * @throws IOException Some I/O error occurred when posting measurements to SNAMP.
     */
    @Override
    public void report(final Measurement... measurements) throws IOException {
        for (final Measurement m : measurements)
            output.println(m.toJsonString(true));
    }

    /**
     * Nothing to do.
     */
    @Override
    public void close() {

    }
}
