package com.bytex.jcommands;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

/**
 * Represents command execution channel.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface CommandExecutionChannel extends Closeable {

    /**
     * Gets channel processing mode supported by this channel.
     * @return Supported channel processing mode.
     */
    ChannelProcessingMode getProcessingMode();

    /**
     * Gets a set of supported processing modes.
     * @return A set of supported processing modes.
     */
    Set<ChannelProcessingMode> getSupportedProcessingModes();

    /**
     * Sets channel processing mode.
     * @param value The processing mode.
     * @throws java.lang.IllegalArgumentException Unsupported processing mode.
     * @see #getSupportedProcessingModes()
     */
    void setProcessingMode(final ChannelProcessingMode value);

    /**
     * Executes the specified action in the channel context.
     * @param command The command to apply in channel context.
     * @param input The additional input for command renderer.
     * @param <I> Type of the user-defined input for the command renderer.
     * @param <O> Type of the execution result.
     * @param <E> Type of the non-I/O exception occurred in the command.
     * @return The execution result.
     * @throws IOException Some I/O error occurs in the channel.
     * @throws E Non-I/O exception raised by the command.
     */
    <I, O, E extends Exception> O exec(final ChannelProcessor<I, O, E> command, final I input) throws IOException, E;
}
