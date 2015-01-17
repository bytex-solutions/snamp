package com.itworks.snamp.testing.connectors.rshell;

import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.ops4j.pax.exam.options.AbstractProvisionOption;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Objects;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractRShellConnectorTest extends AbstractResourceConnectorTest {
    protected static final String CONNECTOR_NAME = "rshell";
    private final SshServer server;
    private final int port;

    protected AbstractRShellConnectorTest(final String sshUserName,
                                          final String password,
                                          final int port,
                                          final String certificateFile,
                                          final String fingerprint,
                                          final AbstractProvisionOption<?>... deps) {
        super(CONNECTOR_NAME, getConnectionString(port), new HashMap<String, String>(){{
                    put("host", "localhost");
                    put("port", Integer.toString(port));
                    put("fingerprint", fingerprint);
                    put("userName", sshUserName);
                    put("password", password);
                }},
                concat(deps,
                        mavenBundle("org.apache.sshd", "sshd-core", "0.12.0"),
                        SnampFeature.SSHJ.getReference(),
                        SnampFeature.RSHELL_CONNECTOR.getReference()));
        server = SshServer.setUpDefaultServer();
        server.setPort(this.port = port);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(certificateFile));
        server.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(final String username, final String pwd, final ServerSession session) {
                return Objects.equals(username, sshUserName) &&
                        Objects.equals(password, pwd);
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

    protected static String getConnectionString(final int port){
        return String.format("ssh://localhost:%s", port);
    }

    protected final String getConnectionString(){
        return getConnectionString(port);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        server.start();
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        server.stop();
    }
}
