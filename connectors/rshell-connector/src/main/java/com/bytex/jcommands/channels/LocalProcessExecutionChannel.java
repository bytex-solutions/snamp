package com.bytex.jcommands.channels;

import com.bytex.jcommands.ChannelProcessingMode;
import com.bytex.jcommands.ChannelProcessor;
import com.bytex.jcommands.CommandExecutionChannel;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.exec.ExecuteException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.bytex.snamp.MapUtils.getValueAsInt;
import static com.bytex.snamp.MapUtils.putIntValue;

/**
 * Represents channel that executes commands as processes in
 * the local OS. This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class LocalProcessExecutionChannel extends HashMap<String, String> implements CommandExecutionChannel, SafeCloseable {
    public static final String CHANNEL_NAME = "process";
    private static final String NORMAL_EXIT_CODE_PARAM = "normalExitCode";
    private static final long serialVersionUID = 5308027932652020638L;

    private transient final Runtime rt = Runtime.getRuntime();

    public LocalProcessExecutionChannel(final Map<String, String> params){
        super(params);
    }

    public LocalProcessExecutionChannel(final int normalExitCode) {
        this(ImmutableMap.of(NORMAL_EXIT_CODE_PARAM, Integer.toString(normalExitCode)));
    }

    int getNormalExitCode() {
        return getValueAsInt(this, NORMAL_EXIT_CODE_PARAM, Integer::parseInt).orElse(0);
    }

    void setNormalExitCode(final int value) {
        putIntValue(this, NORMAL_EXIT_CODE_PARAM, value, Integer::toString);
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
     * {@link com.bytex.jcommands.ChannelProcessingMode#CONNECTION_PER_EXECUTION}.
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
        final Process proc = rt.exec(command.renderCommand(obj, this));
        try (final Reader input = new InputStreamReader(proc.getInputStream(), IOUtils.DEFAULT_CHARSET);
             final Reader error = new InputStreamReader(proc.getErrorStream(), IOUtils.DEFAULT_CHARSET)) {
            final int processExitCode = proc.waitFor();
            final String result = IOUtils.toString(input);
            final String err = IOUtils.toString(error);
            return err.length() > 0 || processExitCode != getNormalExitCode() ?
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
