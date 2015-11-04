package com.bytex.jcommands.channels;

import com.bytex.jcommands.ChannelProcessingMode;
import com.bytex.jcommands.ChannelProcessor;
import com.bytex.jcommands.CommandExecutionChannel;
import com.bytex.snamp.SafeCloseable;
import net.schmizz.sshj.common.IOUtils;
import org.apache.commons.net.bsd.RExecClient;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents execution channel that uses rexec protocol.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RExecExecutionChannel extends HashMap<String, String> implements CommandExecutionChannel, SafeCloseable {
    public static final String CHANNEL_NAME = "rexec";
    private static final String LOCAL_USER_PROPERTY = "remoteUser";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String REMOTE_HOST_PROPERTY = "host";
    private static final String REMOTE_PORT_PROPERTY = "port";
    private static final int DEFAULT_PORT = 514;
    private static final long serialVersionUID = 6937841976838538881L;

    public RExecExecutionChannel(final Map<String, String> params){
        super(params);
    }

    public RExecExecutionChannel(final URI connectionString, final Map<String, String> params) {
        this(params);
        put(REMOTE_HOST_PROPERTY, connectionString.getHost());
        put(REMOTE_PORT_PROPERTY, Integer.toString(connectionString.getPort()));
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
     * @return A set of supported processing modes.
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
        if (!getSupportedProcessingModes().contains(value))
            throw new IllegalArgumentException(String.format("Channel mode %s is not supported.", value));
    }

    /**
     * Executes the specified action in the channel context.
     *
     * @param command The command to apply in channel context.
     * @param input The additional input for command renderer.
     * @return The execution result.
     * @throws java.io.IOException Some I/O error occurs in the channel.
     * @throws E           Non-I/O exception raised by the command.
     */
    @Override
    public <I, O, E extends Exception> O exec(final ChannelProcessor<I, O, E> command, final I input) throws IOException, E {
        final RExecClient client = new RExecClient();
        client.connect(get(REMOTE_HOST_PROPERTY),
                containsKey(REMOTE_PORT_PROPERTY) ? Integer.parseInt(get(REMOTE_HOST_PROPERTY)) : DEFAULT_PORT);
        try {
            client.rexec(get(LOCAL_USER_PROPERTY), get(PASSWORD_PROPERTY), command.renderCommand(input, this), true);
            final String result = IOUtils.readFully(client.getInputStream()).toString("UTF-8");
            final String err = IOUtils.readFully(client.getErrorStream()).toString("UTF-8");
            return command.process(result, err == null || err.isEmpty() ? null : new IOException(err));
        } finally {
            client.disconnect();
        }
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     */
    @Override
    public void close() {

    }
}
