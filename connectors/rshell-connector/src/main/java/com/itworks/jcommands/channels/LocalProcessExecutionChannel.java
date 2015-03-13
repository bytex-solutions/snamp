package com.itworks.jcommands.channels;

import com.google.common.collect.ImmutableMap;
import com.itworks.jcommands.ChannelProcessingMode;
import com.itworks.jcommands.ChannelProcessor;
import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.MethodStub;
import org.apache.commons.exec.ExecuteException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents channel that executes commands as processes in
 * the local OS. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class LocalProcessExecutionChannel extends HashMap<String, String> implements CommandExecutionChannel {
    static final String CHANNEL_NAME = "process";
    private static final String NORMAL_EXIT_CODE_PARAM = "normalExitCode";
    private static final long serialVersionUID = 5308027932652020638L;

    private final Runtime rt;

    LocalProcessExecutionChannel(final Map<String, String> params){
        super(params);
        rt = Runtime.getRuntime();
    }

    LocalProcessExecutionChannel(final int normalExitCode) {
        this(ImmutableMap.of(NORMAL_EXIT_CODE_PARAM, Integer.toString(normalExitCode)));
    }

    int getNormalExitCode() {
        if (containsKey(NORMAL_EXIT_CODE_PARAM))
            return Integer.valueOf(get(NORMAL_EXIT_CODE_PARAM));
        else if (Utils.IS_OS_LINUX)
            return 0;
        else if (Utils.IS_OS_WINDOWS)
            return 0;
        else return 0;
    }

    void setNormalExitCode(final int value){
        put(NORMAL_EXIT_CODE_PARAM, Integer.toString(value));
    }

    /**
     * Gets channel processing mode supported by this channel.
     *
     * @return Supported channel processing mode.
     */
    @Override
    public ChannelProcessingMode getProcessingMode() {
        return ChannelProcessingMode.CONNECTION_PER_EXECUTION;
    }

    /**
     * Gets a set of supported processing modes.
     *
     * @return A set consists of single value
     * {@link com.itworks.jcommands.ChannelProcessingMode#CONNECTION_PER_EXECUTION}.
     */
    @Override
    public Set<ChannelProcessingMode> getSupportedProcessingModes() {
        return EnumSet.of(ChannelProcessingMode.CONNECTION_PER_EXECUTION);
    }

    /**
     * Sets channel processing mode.
     *
     * @param value The processing mode.
     * @throws IllegalArgumentException Unsupported processing mode.
     * @see #getSupportedProcessingModes()
     */
    @Override
    public void setProcessingMode(final ChannelProcessingMode value) {
        if(value != ChannelProcessingMode.CONNECTION_PER_EXECUTION)
            throw new IllegalArgumentException(String.format("Unsupported processing mode %s", value));
    }

    private static String toString(final Reader reader) throws IOException {
        final StringBuilder result = new StringBuilder();
        while (reader.ready()){
            final char[] buffer = new char[10];
            final int count = reader.read(buffer);
            result.append(buffer, 0, count);
        }
        return result.toString();
    }

    /**
     * Executes the specified action in the channel context.
     *
     * @param command The command to apply in channel context.
     * @param obj Additional input for the command renderer.
     * @return The execution result.
     * @throws java.io.IOException Some I/O error occurs in the channel.
     * @throws E           Non-I/O exception raised by the command.
     */
    @Override
    public <I, O, E extends Exception> O exec(final ChannelProcessor<I, O, E> command,
                                           final I obj) throws IOException, E {

        // On OS of Windows family we have to set interpreter before the command
        final String commandString = com.itworks.snamp.internal.Utils.IS_OS_LINUX ? "" : "cmd /c " +
                command.renderCommand(obj, this);
        final Process proc = rt.exec(commandString);
        try (final Reader input = new InputStreamReader(proc.getInputStream());
             final Reader error = new InputStreamReader(proc.getErrorStream())) {
            final int processExitCode = proc.waitFor();
            final String result = toString(input);
            final String err = toString(error);
            return err != null && err.length() > 0 || processExitCode != getNormalExitCode() ?
                    command.process(result, new ExecuteException(err, processExitCode)) :
                    command.process(result, null);
        } catch (final InterruptedException e) {
            throw new IOException(e);
        } finally {
            proc.destroy();
        }
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     */
    @Override
    @MethodStub
    public void close() {
    }
}
