package com.itworks.snamp.adapters.ssh;

import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

/**
 * Represents form of the attribute suitable for printing via text-based streams.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SshAttributeView {
    void printValue(final PrintWriter output) throws TimeoutException;
    void printOptions(final PrintWriter output);

    String getName();
}
