package com.bytex.jcommands;

import com.bytex.jcommands.channels.CommandExecutionChannels;
import com.bytex.jcommands.channels.LocalProcessExecutionChannel;
import com.bytex.jcommands.impl.XmlCommandLineTemplate;
import com.bytex.jcommands.impl.XmlParserDefinition;
import com.bytex.jcommands.impl.XmlParsingResultType;
import com.bytex.snamp.internal.OperatingSystem;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class LocalProcessExecutionChannelTest extends Assert {
    @Test
    public void instantiationTest() throws Exception{
        try(final CommandExecutionChannel channel = CommandExecutionChannels.createChannel(LocalProcessExecutionChannel.CHANNEL_NAME, Collections.emptyMap())){
            assertNotNull(channel);
        }
    }

    @Test
    public void echoTest() throws Exception {
        Assume.assumeTrue("Linux-specific test", OperatingSystem.isLinux());
        try(final CommandExecutionChannel channel = CommandExecutionChannels.createLocalProcessExecutionChannel()){
            final String str = "Hello, world!";
            final String result = channel.exec(new ChannelProcessor<String, String, Exception>() {
                @Override
                public String renderCommand(final String str, final Map<String, ?> channelParameters) {
                    return String.format("echo %s", str);
                }

                @Override
                public String process(final String result, final Exception error) {
                    assertNull(error);
                    return result;
                }
            }, str);
            assertTrue(result.startsWith(str));
        }
    }

    @Test
    public void freeMemTest() throws IOException, ScriptException {
        Assume.assumeTrue("Linux-specific test", OperatingSystem.isLinux());
        final XmlCommandLineTemplate template = new XmlCommandLineTemplate();
        template.setCommandTemplate("free {format}");
        template.getCommandOutputParser().setParsingLanguage(XmlParserDefinition.REGEXP_LANG);
        template.getCommandOutputParser().setParsingResultType(XmlParsingResultType.DICTIONARY);
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-z]+(/[a-z]+)?");
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-zA-Z]+\\:");
        template.getCommandOutputParser().addDictionaryEntryRule("total", "[0-9]+", XmlParsingResultType.INTEGER);
        template.getCommandOutputParser().addDictionaryEntryRule("used", "[0-9]+", XmlParsingResultType.INTEGER);
        template.getCommandOutputParser().addDictionaryEntryRule("free", "[0-9]+", XmlParsingResultType.INTEGER);
        final CommandExecutionChannel channel = CommandExecutionChannels.createLocalProcessExecutionChannel(ImmutableMap.of(
            "format", "-m"
        ));
        final Object memStatus = channel.exec(template, Collections.emptyMap());
        assertTrue(memStatus instanceof Map);
        assertEquals(3, ((Map)memStatus).size());
        assertTrue(((Map)memStatus).get("total") instanceof Integer);
    }
}
