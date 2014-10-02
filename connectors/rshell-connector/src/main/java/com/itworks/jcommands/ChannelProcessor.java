package com.itworks.jcommands;

import java.util.Map;

/**
 * Represents functional interface that is used to process channel.
 * @param <I> Type of the user defined input for the command renderer.
 * @param <O> Type of the processing result.
 * @param <E> Type of the exception that can be thrown by processor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ChannelProcessor<I, O, E extends Exception> {
    /**
     * Creates a textual command to be executed through the channel.
     * @param input User-define input for the command renderer.
     * @param channelParameters The channel initialization parameters. Cannot be {@literal null}.
     * @return The command to execute.
     */
    String renderCommand(final I input, final Map<String, ?> channelParameters);

    /**
     * Processes the command execution result.
     * @param result The result to parse.
     * @param error The error message.
     * @return Processing result.
     * @throws E Some non I/O processing exception.
     */
    O process(final String result, final Exception error) throws E;
}
