package com.bytex.jcommands;

import com.google.common.collect.ImmutableMap;
import com.bytex.jcommands.channels.CommandExecutionChannels;
import com.bytex.jcommands.impl.XmlCommandLineTemplate;
import com.bytex.jcommands.impl.XmlParserDefinition;
import com.bytex.jcommands.impl.XmlParsingResultType;
import com.bytex.snamp.internal.Utils;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.junit.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SSHExecutionChannelTest extends Assert {
    private final SshServer server;
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;

    public SSHExecutionChannelTest() {
        server = SshServer.setUpDefaultServer();
        server.setPort(PORT);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(System.getProperty("ssh-cert-file", "hostkey.ser")));
        server.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(final String username, final String password, final ServerSession session) {
                return Objects.equals(username, USER_NAME) &&
                        Objects.equals(password, PASSWORD);
            }
        });
        server.setCommandFactory(new CommandFactory() {
            @Override
            public Command createCommand(final String command) {
                final ProcessShellFactory factory = new ProcessShellFactory(command.split(" "));
                return factory.create();
            }
        });
    }

    @Before
    public void startServer() throws IOException {
        server.start();
    }

    @After
    public void stopServer() throws InterruptedException {
        server.stop();
    }

    @Test
    public void executeFreeCommandWithParsingTest() throws Exception{
        Assume.assumeTrue(Utils.IS_OS_LINUX);
        final CommandExecutionChannel channel = CommandExecutionChannels.createChannel("ssh", ImmutableMap.of(
            "userName", USER_NAME,
            "password", PASSWORD,
            "port", Integer.toString(PORT),
            "fingerprint", "e8:0d:af:84:bb:ec:05:03:b9:7c:f3:75:19:5a:2a:63",
            "format", "-m"
            ));
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
        assertNotNull(channel);
        final Object memStatus = channel.exec(template, Collections.<String, Object>emptyMap());
        assertTrue(memStatus instanceof Map);
        assertEquals(3, ((Map)memStatus).size());
        assertTrue(((Map)memStatus).get("total") instanceof Integer);
    }

    @Test
    public void executeFreeCommandWithoutParsingTest() throws Exception {
        Assume.assumeTrue(Utils.IS_OS_LINUX);
        final CommandExecutionChannel channel = CommandExecutionChannels.createChannel("ssh", ImmutableMap.of(
                "userName", USER_NAME,
            "password", PASSWORD,
            "port", Integer.toString(PORT),
            "fingerprint", "e8:0d:af:84:bb:ec:05:03:b9:7c:f3:75:19:5a:2a:63"
        ));
        assertNotNull(channel);
        final String result = channel.exec(new ChannelProcessor<Void, String, Exception>() {
            @Override
            public String renderCommand(final Void stub,
                                        final Map<String, ?> channelParameters) {
                return "free -m";
            }

            @Override
            public String process(final String result, final Exception error) throws Exception {
                assertNull(error);
                return result;
            }
        }, null);
        assertNotNull(result);
    }
}
