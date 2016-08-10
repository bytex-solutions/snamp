package com.bytex.snamp.testing.connector.rshell;

import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.osgi.framework.BundleContext;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.RSHELL_CONNECTOR)
public abstract class AbstractRShellConnectorTest extends AbstractResourceConnectorTest {
    protected static final String CONNECTOR_NAME = "rshell";
    private SshServer server;
    private final int port;
    private final String certificateFile;
    private final String sshUserName;
    private final String password;

    protected AbstractRShellConnectorTest(final String sshUserName,
                                          final String password,
                                          final int port,
                                          final String certificateFile,
                                          final String fingerprint) {
        super(CONNECTOR_NAME, getConnectionString(port), ImmutableMap.of(
                    "host", "localhost",
                    "port", Integer.toString(port),
                    "fingerprint", fingerprint,
                    "userName", sshUserName,
                    "password", password
        ));
        this.certificateFile = certificateFile;
        this.port = port;
        this.sshUserName = sshUserName;
        this.password = password;
    }

    protected static String getConnectionString(final int port){
        return String.format("ssh://localhost:%s", port);
    }

    protected final String getConnectionString(){
        return getConnectionString(port);
    }

    /*@Override
    protected boolean enableRemoteDebugging() {
        return true;
    } */

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        final SshServer server = this.server = SshServer.setUpDefaultServer();
        server.setPort(port);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(certificateFile));
        server.setPasswordAuthenticator((username, pwd, session) -> Objects.equals(username, sshUserName) &&
                Objects.equals(password, pwd));
        server.setCommandFactory(command -> {
            final ProcessShellFactory factory = new ProcessShellFactory(command.split(" "));
            return factory.create();
        });
        server.start();
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        server.stop();
        server = null;
    }
}
