package com.itworks.jcommands;

import java.io.*;
import java.util.Map;

/**
 * Represents command.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface Command {
    /**
     * Constructs command line using command arguments.
     * @param arguments Command arguments.
     * @param output Output stream that receives command line.
     * @param input Input stream that contains execution result.
     * @param err Input stream that contains error messages.
     */
    Object execute(final Map<String, String> arguments,
                   final PrintStream output,
                   final InputStream input,
                   final InputStream err) throws IOException, CommandExecutionException;
}
