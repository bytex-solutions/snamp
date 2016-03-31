package com.bytex.jcommands.channels;

import com.bytex.jcommands.CommandExecutionChannel;
import com.bytex.jcommands.channels.spi.CommandExecutionChannelSpi;
import com.bytex.jcommands.channels.spi.URICommandExecutionChannelSpi;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Represents consolidated factory of
 * {@link com.bytex.jcommands.CommandExecutionChannel} implementations.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class CommandExecutionChannels {
    private CommandExecutionChannels(){

    }

    private static ServiceLoader<CommandExecutionChannelSpi> getFactories(){
        return ServiceLoader.load(CommandExecutionChannelSpi.class, CommandExecutionChannels.class.getClassLoader());
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
        for(final CommandExecutionChannelSpi factory: getFactories())
            if(Objects.equals(channelType, factory.getType()))
                return factory.create(params);
        return null;
    }

    /**
     * Creates a new execution channel that supports initialization from {@link java.net.URI} object.
     * @param connectionString The connection string used to initialize the channel. Cannot be {@literal null}.
     * @param params Additional channel parameters.
     * @return A new instance of the execution channel; or {@literal null}, if channel doesn't support initialization from {@link java.net.URI}.
     * @throws Exception Unable to instantiate the channel.
     */
    public static CommandExecutionChannel createChannel(final URI connectionString,
                                                        final Map<String, String> params) throws Exception {
        for(final CommandExecutionChannelSpi factory: getFactories())
            if(Objects.equals(connectionString.getScheme(), factory.getType())){
                return factory instanceof URICommandExecutionChannelSpi ?
                        ((URICommandExecutionChannelSpi)factory).create(connectionString, params):
                        factory.create(params);
            }
        return null;
    }

    public static CommandExecutionChannel createLocalProcessExecutionChannel() {
        return new LocalProcessExecutionChannel(Collections.<String, String>emptyMap());
    }

    public static CommandExecutionChannel createLocalProcessExecutionChannel(final Map<String, String> params){
        return new LocalProcessExecutionChannel(params);
    }
}
