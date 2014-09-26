package com.itworks.jcommands.testing;

import com.itworks.jcommands.ChannelProcessor;
import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.jcommands.channels.CommandExecutionChannels;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class LocalProcessExecutionChannelTest extends AbstractUnitTest<CommandExecutionChannel> {
    public LocalProcessExecutionChannelTest() {
        super(CommandExecutionChannel.class);
    }

    @Test
    public void echoTest() throws Exception {
        try(final CommandExecutionChannel channel = CommandExecutionChannels.createLocalProcessExecutionChannel()){
            final String str = "Hello, world!";
            final String result = channel.exec(new ChannelProcessor<String, Exception>() {
                @Override
                public String renderCommand(final Map<String, ?> channelParameters) {
                    return String.format("echo %s", str);
                }

                @Override
                public String process(final String result, final Exception error) {
                    assertNull(error);
                    return result;
                }
            });
            assertTrue(result.startsWith(str));
        }
    }
}
