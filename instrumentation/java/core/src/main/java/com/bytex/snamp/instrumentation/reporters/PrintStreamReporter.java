package com.bytex.snamp.instrumentation.reporters;

import com.bytex.snamp.instrumentation.measurements.Measurement;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

/**
 * Represents reporter that writes all incoming measurements into print stream.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class PrintStreamReporter implements Reporter {
    private final PrintStream output;
    private final boolean autoClose;

    public PrintStreamReporter(final PrintStream output, final boolean autoClose){
        this.output = Objects.requireNonNull(output);
        this.autoClose = autoClose;
    }

    public static PrintStreamReporter toStandardOutput(){
        return new PrintStreamReporter(System.out, false);
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
        if(autoClose)
            output.close();
    }
}
