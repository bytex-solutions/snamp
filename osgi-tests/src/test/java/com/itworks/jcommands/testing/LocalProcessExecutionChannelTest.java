package com.itworks.jcommands.testing;

import com.itworks.jcommands.ChannelProcessor;
import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.jcommands.channels.CommandExecutionChannels;
import com.itworks.jcommands.impl.XmlCommandLineTemplate;
import com.itworks.jcommands.impl.XmlParserDefinition;
import com.itworks.jcommands.impl.XmlParsingResultType;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
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

    @Test
    public void freeMemTest() throws IOException, ScriptException {
        Assume.assumeTrue(SystemUtils.IS_OS_LINUX);
        final XmlCommandLineTemplate template = new XmlCommandLineTemplate();
        template.setCommandTemplate("free {format}");
        template.getCommandOutputParser().setParsingLanguage(XmlParserDefinition.REGEXP_LANG);
        template.getCommandOutputParser().setParsingResultType(XmlParsingResultType.DICTIONARY);
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-z]+");
        template.getCommandOutputParser().addParsingRule("[a-zA-Z]+\\:");
        template.getCommandOutputParser().addDictionaryEntryRule("total", "[0-9]+", XmlParsingResultType.INTEGER);
        template.getCommandOutputParser().addDictionaryEntryRule("used", "[0-9]+", XmlParsingResultType.INTEGER);
        template.getCommandOutputParser().addDictionaryEntryRule("free", "[0-9]+", XmlParsingResultType.INTEGER);
        final CommandExecutionChannel channel = CommandExecutionChannels.createLocalProcessExecutionChannel(new HashMap<String, String>(1){{
            put("format", "-m");
        }});
        final Object memStatus = channel.exec(template);
        assertTrue(memStatus instanceof Map);
        assertEquals(3, ((Map)memStatus).size());
        assertTrue(((Map)memStatus).get("total") instanceof Integer);
    }
}
