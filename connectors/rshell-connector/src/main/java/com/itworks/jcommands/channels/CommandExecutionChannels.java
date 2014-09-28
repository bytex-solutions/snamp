package com.itworks.jcommands.channels;

import com.itworks.jcommands.CommandExecutionChannel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents consolidated factory of
 * {@link com.itworks.jcommands.CommandExecutionChannel} implementations.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class CommandExecutionChannels {
    /**
     * Represents producer of {@link com.itworks.jcommands.CommandExecutionChannel} implementation.
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static interface ExecutionChannelProducer {
        /**
         * Creates a new instance of the execution channel.
         * @param params Channel initialization parameters. Cannot be {@literal null}.
         * @return A new instance of the execution channel.
         * @throws java.lang.Exception Unable to instantiate channel.
         */
        CommandExecutionChannel produce(final Map<String, String> params) throws Exception;
    }

    private static final Map<String, ExecutionChannelProducer> channels;

    static {
        channels = new HashMap<>(3);
        channels.put(LocalProcessExecutionChannel.CHANNEL_NAME, new ExecutionChannelProducer() {
            @Override
            public CommandExecutionChannel produce(final Map<String, String> params) {
                return new LocalProcessExecutionChannel(params);
            }
        });
        channels.put(SSHExecutionChannel.CHANNEL_NAME, new ExecutionChannelProducer() {
            @Override
            public CommandExecutionChannel produce(final Map<String, String> params) throws Exception {
                return new SSHExecutionChannel(params);
            }
        });
    }

    public static void registerChannelFactory(final String[] channelTypes,
                                                 final ExecutionChannelProducer channelFactory) {
        if (channelFactory == null) throw new IllegalArgumentException("channelFactory is null.");
        else for(final String type: channelTypes)
            channels.put(type, channelFactory);
    }

    /**
     * Creates a new instance of the specified command execution channel.
     * @param channelType The name of the channel to create.
     * @param params Channel initialization parameters. Cannot be {@literal null}.
     * @return A new instance of the channel; or {@literal null}, if the specified channel
     * doesn't exist.
     * @throws java.lang.Exception Some error occurred during channel instantiation.
     */
    public static CommandExecutionChannel createChannel(final String channelType,
                                                        final Map<String, String> params) throws Exception{
        if (channels.containsKey(channelType)) {
            final ExecutionChannelProducer producer = channels.get(channelType);
            return producer != null ? producer.produce(params) : null;
        } else return null;
    }

    public static CommandExecutionChannel createLocalProcessExecutionChannel() {
        return new LocalProcessExecutionChannel(Collections.<String, String>emptyMap());
    }

    public static CommandExecutionChannel createLocalProcessExecutionChannel(final Map<String, String> params){
        return new LocalProcessExecutionChannel(params);
    }
}
