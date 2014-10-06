package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.adapters.AbstractResourceAdapter;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.jaas.JaasPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * Represents SSH resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshAdapter extends AbstractResourceAdapter implements AdapterController {
    static final String NAME = SshHelpers.ADAPTER_NAME;
    private final SshServer server;
    private final ExecutorService commandExecutors;

    SshAdapter(final String host,
               final int port,
               final String serverCertificateFile,
               final SshSecuritySettings security,
               final Map<String, ManagedResourceConfiguration> resources) {
        super(resources);
        server = SshServer.setUpDefaultServer();
        server.setHost(host);
        server.setPort(port);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(serverCertificateFile));
        setupSecurity(server, security);
        commandExecutors = Executors.newCachedThreadPool();

    }

    private static void setupSecurity(final SshServer server, final SshSecuritySettings security) {
        if (security.hasJaasDomain()) {
            final JaasPasswordAuthenticator auth = new JaasPasswordAuthenticator();
            auth.setDomain(security.getJaasDomain());
            server.setPasswordAuthenticator(auth);
        } else if (security.hasUserCredentials())
            server.setPasswordAuthenticator(new PasswordAuthenticator() {
                private final String userName = security.getUserName();
                private final String password = security.getPassword();

                @Override
                public boolean authenticate(final String username, final String password, final ServerSession session) {
                    return Objects.equals(username, this.userName) && Objects.equals(password, this.password);
                }
            });
    }

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    protected boolean start() {
        try {
            server.setCommandFactory(new ManagementShell(this));
            server.start();
            return true;
        } catch (final IOException e) {
            getLogger().log(Level.SEVERE, String.format("Unable to start SSH adapter"), e);
            return false;
        }
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop() {
        try {
            server.stop();
        }
        catch (final InterruptedException e) {
            getLogger().log(Level.SEVERE, String.format("Unable to stop SSH adapter"), e);
        }
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public Set<String> getConnectedResources() {
        return getHostedResources();
    }

    @Override
    public ExecutorService getCommandExecutorService() {
        return commandExecutors;
    }
}
