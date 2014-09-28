package com.itworks.jcommands.channels;

import com.itworks.jcommands.ChannelProcessingMode;
import com.itworks.jcommands.ChannelProcessor;
import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.snamp.internal.MapBuilder;
import com.itworks.snamp.internal.annotations.MethodStub;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

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

    private final Runtime rt;

    public LocalProcessExecutionChannel(final Map<String, String> params){
        super(params);
        rt = Runtime.getRuntime();
    }

    public LocalProcessExecutionChannel(final int normalExitCode) {
        this(MapBuilder.create(NORMAL_EXIT_CODE_PARAM, Integer.toString(normalExitCode), 1).getMap());
    }

    public int getNormalExitCode() {
        if (containsKey(NORMAL_EXIT_CODE_PARAM))
            return Integer.valueOf(get(NORMAL_EXIT_CODE_PARAM));
        else if (SystemUtils.IS_OS_LINUX)
            return 0;
        else if (SystemUtils.IS_OS_WINDOWS)
            return 0;
        else return 0;
    }

    public void setNormalExitCode(final int value){
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
     * @param command The command to execute in channel context.
     * @return The execution result.
     * @throws java.io.IOException Some I/O error occurs in the channel.
     * @throws E           Non-I/O exception raised by the command.
     */
    @Override
    public <T, E extends Exception> T exec(final ChannelProcessor<T, E> command) throws IOException, E {
        final Process proc = rt.exec(command.renderCommand(this));
        try (final Reader input = new InputStreamReader(proc.getInputStream());
             final Reader error = new InputStreamReader(proc.getErrorStream())) {
            final String result = toString(input);
            final String err = toString(error);
            final int processExitCode = proc.waitFor();
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
