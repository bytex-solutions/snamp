package com.itworks.jcommands.channels;

import com.itworks.jcommands.ChannelProcessingMode;
import com.itworks.jcommands.ChannelProcessor;
import com.itworks.jcommands.CommandExecutionChannel;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Represents command execution channel that uses SSH connection for executing
 * commands on the remote machine.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SSHExecutionChannel extends SSHClient implements CommandExecutionChannel {
    static final String CHANNEL_NAME = "ssh";

    private static interface Authenticator{
        void authenticate(final SSHClient client) throws UserAuthException, TransportException;
    }

    private static final String KNOWN_HOSTS_PROPERTY = "knownHosts";

    private static final String HOST_NAME_PROPERTY = "host";
    private static final String DEFAULT_HOST_NAME = "localhost";

    private static final String PORT_NAME_PROPERTY = "port";
    private static final String LOCAL_HOST_NAME_PROPERTY = "localHost";
    private static final String LOCAL_PORT_NAME_PROPERTY = "localPort";

    private static final String SOCKET_TIMEOUT_PROPERTY = "socketTimeout";
    private static final String ENCODING_PROPERTY = "encoding";

    private static final String SSH_KEY_FILE_PROPERTY = "sshKeyFile";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String USER_NAME_PROPERTY = "userName";
    private static final String FINGERPRINT_PROPERTY = "fingerprint";

    private final String remoteHost;
    private final int remotePort;
    private final InetAddress localHost;
    private final int localPort;
    private final Map<String, String> channelParams;
    private final String encoding;
    private ChannelProcessingMode mode;
    private Session session;
    private final Authenticator auth;

    public SSHExecutionChannel(final Map<String, String> channelParams) throws IOException {
        mode = ChannelProcessingMode.SINGLETON_CONNECTION;
        session = null;
        if (channelParams.containsKey(KNOWN_HOSTS_PROPERTY))
            loadKnownHosts(this, channelParams.get(KNOWN_HOSTS_PROPERTY));
        remoteHost = Objects.toString(channelParams.get(HOST_NAME_PROPERTY), DEFAULT_HOST_NAME);
        remotePort = Integer.parseInt(Objects.toString(channelParams.get(PORT_NAME_PROPERTY), Integer.toString(DEFAULT_PORT)));
        if (channelParams.containsKey(LOCAL_HOST_NAME_PROPERTY)) {
            localHost = InetAddress.getByName(Objects.toString(channelParams.get(LOCAL_HOST_NAME_PROPERTY)));
            localPort = Integer.parseInt(Objects.toString(channelParams.get(LOCAL_PORT_NAME_PROPERTY), "30000"));
        } else {
            localHost = null;
            localPort = -1;
        }
        if (channelParams.containsKey(SOCKET_TIMEOUT_PROPERTY)) {
            final int timeoutMillis = Integer.parseInt(Objects.toString(channelParams.get(SOCKET_TIMEOUT_PROPERTY)));
            setTimeout(timeoutMillis);
            setConnectTimeout(timeoutMillis);
        }
        if(channelParams.containsKey(FINGERPRINT_PROPERTY))
            addHostKeyVerifier(channelParams.get(FINGERPRINT_PROPERTY));
        encoding = Objects.toString(channelParams.get(ENCODING_PROPERTY), Charset.defaultCharset().name());
        if (channelParams.containsKey(USER_NAME_PROPERTY))
            if (channelParams.containsKey(PASSWORD_PROPERTY))
                auth = new Authenticator() {
                    private final String userName = channelParams.get(USER_NAME_PROPERTY);
                    private final String password = channelParams.get(PASSWORD_PROPERTY);

                    @Override
                    public void authenticate(final SSHClient client) throws UserAuthException, TransportException {
                        client.authPassword(userName, password);
                    }
                };
            else if (channelParams.containsKey(SSH_KEY_FILE_PROPERTY))
                auth = new Authenticator() {
                    private final String userName = channelParams.get(USER_NAME_PROPERTY);
                    private final String keyFile = channelParams.get(SSH_KEY_FILE_PROPERTY);

                    @Override
                    public void authenticate(final SSHClient client) throws UserAuthException, TransportException {
                        client.authPublickey(userName, keyFile);
                    }
                };
            else auth = new Authenticator() {
                    private final String userName = channelParams.get(USER_NAME_PROPERTY);

                    @Override
                    public void authenticate(final SSHClient client) throws UserAuthException, TransportException {
                        client.authPublickey(userName);
                    }
                };
        else auth = null;
        this.channelParams = safeChannelParams(channelParams);
    }

    private static Map<String, String> safeChannelParams(final Map<String, String> params){
        final Map<String, String> result = new HashMap<>(params);
        result.remove(PASSWORD_PROPERTY);
        result.remove(SSH_KEY_FILE_PROPERTY);
        return result;
    }

    private static void loadKnownHosts(final SSHClient client, final Object hostsFile) throws IOException {
        if(hostsFile instanceof File)
            client.loadKnownHosts((File)hostsFile);
        else if(hostsFile instanceof String)
            client.loadKnownHosts(new File((String)hostsFile));
    }

    /**
     * Gets channel processing mode supported by this channel.
     *
     * @return Supported channel processing mode.
     */
    @Override
    public ChannelProcessingMode getProcessingMode() {
        return ChannelProcessingMode.SINGLETON_CONNECTION;
    }

    /**
     * Gets a set of supported processing modes.
     *
     * @return A set of supported processing modes.
     */
    @Override
    public Set<ChannelProcessingMode> getSupportedProcessingModes() {
        return EnumSet.of(ChannelProcessingMode.SINGLETON_CONNECTION);
    }

    /**
     * Sets channel processing mode.
     *
     * @param value The processing mode.
     * @throws IllegalArgumentException Unsupported processing mode.
     * @see #getSupportedProcessingModes()
     */
    @Override
    public synchronized void setProcessingMode(final ChannelProcessingMode value) {
        if (getSupportedProcessingModes().contains(value)) {
            this.mode = value;
            if (session != null) try {
                session.close();
            } catch (final IOException ignored) {
            }
        } else throw new IllegalArgumentException(String.format("Channel mode %s is not supported", value));
    }

    private static <T, E extends Exception> T exec(final Session s,
                                                   final String encoding,
                                                   final Map<String, ?> channelParams,
                                                   final ChannelProcessor<T, E> command) throws IOException, E{
        final Session.Command result = s.exec(command.renderCommand(channelParams));
        final String out = IOUtils.readFully(result.getInputStream()).toString(encoding);
        String err = IOUtils.readFully(result.getErrorStream()).toString(encoding);
        if(err == null || err.isEmpty()) err = result.getExitErrorMessage();
        return command.process(out, err == null || err.isEmpty() ? null : new SSHException(err));
    }

    /**
     * Executes the specified action in the channel context.
     *
     * @param command The command to execute in channel context.
     * @return The execution result.
     * @throws java.io.IOException Some I/O error occurs in the channel.
     * @throws E                   Non-I/O exception raised by the command.
     */
    @Override
    public synchronized <T, E extends Exception> T exec(final ChannelProcessor<T, E> command) throws IOException, E {
        if (isConnected())
            switch (mode) {
                case CONNECTION_PER_EXECUTION:
                    try (final Session s = startSession()) {
                        return exec(s, encoding, channelParams, command);
                    }
                case SINGLETON_CONNECTION:
                    if (session == null) { //starts a new session
                        session = startSession();
                        return exec(command);
                    } else if (session.isOpen()) {
                        return exec(session, encoding, channelParams, command);
                    } else {//close dead session and initialize a new session
                        session.close();
                        session = null;
                        return exec(command);
                    }
                default:
                    throw new IOException(String.format("Unsupported channel mode %s.", mode));
            }
        else if (localHost == null) {
            connect(remoteHost, remotePort);
            if (auth != null)
                auth.authenticate(this);
            return exec(command);
        } else {
            connect(remoteHost, remotePort, localHost, localPort);
            if (auth != null)
                auth.authenticate(this);
            return exec(command);
        }
    }
}
