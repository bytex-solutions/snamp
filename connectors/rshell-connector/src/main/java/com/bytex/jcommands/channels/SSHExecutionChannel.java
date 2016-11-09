package com.bytex.jcommands.channels;

import com.bytex.jcommands.ChannelProcessingMode;
import com.bytex.jcommands.ChannelProcessor;
import com.bytex.jcommands.CommandExecutionChannel;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.*;

/**
 * Represents command execution channel that uses SSH connection for executing
 * commands on the remote machine.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SSHExecutionChannel extends SSHClient implements CommandExecutionChannel {
    public static final String CHANNEL_NAME = "ssh";

    private interface Authenticator{
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
    private final ChannelProcessingMode mode;
    private Session session;
    private final Authenticator auth;

    private static Map<String, String> join(final URI connectionString, final Map<String, String> params){
        final Map<String, String> result = new HashMap<>(params);
        putValue(result, HOST_NAME_PROPERTY, connectionString, URI::getHost);
        putIntValue(result, PORT_NAME_PROPERTY, connectionString.getPort(), Integer::toString);
        return result;
    }

    public SSHExecutionChannel(final Map<String, String> params) throws IOException {
        this(params, true);
    }


    public SSHExecutionChannel(final URI connectionString, final Map<String, String> params) throws IOException{
        this(join(connectionString, params), false);
    }

    private static Authenticator fromCredentials(final String userName,
                                                 final String password) {
        return client -> client.authPassword(userName, password);
    }

    private static Authenticator fromKeyFile(final String userName,
                                             final String keyFile) {
        return client -> client.authPublickey(userName, keyFile);
    }

    private static Authenticator fromUserName(final String userName) {
        return client -> client.authPublickey(userName);
    }

    private SSHExecutionChannel(final Map<String, String> params,
                                final boolean copyParams) throws IOException {
        mode = ChannelProcessingMode.SINGLETON_CONNECTION;
        session = null;
        if (params.containsKey(KNOWN_HOSTS_PROPERTY))
            loadKnownHosts(this, params.get(KNOWN_HOSTS_PROPERTY));
        remoteHost = getValue(params, HOST_NAME_PROPERTY, Function.identity(), () -> DEFAULT_HOST_NAME);
        remotePort = getValueAsInt(params, PORT_NAME_PROPERTY, Integer::parseInt, () -> DEFAULT_PORT);
        if (params.containsKey(LOCAL_HOST_NAME_PROPERTY)) {
            localHost = InetAddress.getByName(getValue(params, LOCAL_HOST_NAME_PROPERTY, Function.identity(), () -> "localhost"));
            localPort = getValueAsInt(params, LOCAL_PORT_NAME_PROPERTY, Integer::parseInt, () -> 30000);
        } else {
            localHost = null;
            localPort = -1;
        }
        if (params.containsKey(SOCKET_TIMEOUT_PROPERTY)) {
            final int timeoutMillis = getValueAsInt(params, SOCKET_TIMEOUT_PROPERTY, Integer::parseInt, () -> 0);
            setTimeout(timeoutMillis);
            setConnectTimeout(timeoutMillis);
        }
        if(params.containsKey(FINGERPRINT_PROPERTY))
            addHostKeyVerifier(params.get(FINGERPRINT_PROPERTY));
        encoding = getValue(params, ENCODING_PROPERTY, Function.identity(), Charset.defaultCharset()::name);
        if (params.containsKey(USER_NAME_PROPERTY))
            if (params.containsKey(PASSWORD_PROPERTY))
                auth = fromCredentials(params.get(USER_NAME_PROPERTY), params.get(PASSWORD_PROPERTY));
            else if (params.containsKey(SSH_KEY_FILE_PROPERTY))
                auth = fromKeyFile(params.get(USER_NAME_PROPERTY), params.get(SSH_KEY_FILE_PROPERTY));
            else auth = fromUserName(params.get(USER_NAME_PROPERTY));
        else auth = null;
        this.channelParams = copyParams ? new HashMap<>(params) : params;
        this.channelParams.remove(PASSWORD_PROPERTY);
        this.channelParams.remove(SSH_KEY_FILE_PROPERTY);
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
    public void setProcessingMode(final ChannelProcessingMode value) {
    }

    private static <I, T, E extends Exception> T exec(final Session s,
                                                   final String encoding,
                                                   final Map<String, ?> channelParams,
                                                   final ChannelProcessor<I, T, E> command,
                                                   final I input) throws IOException, E{
        final Session.Command result = s.exec(command.renderCommand(input, channelParams));
        final String out = IOUtils.readFully(result.getInputStream()).toString(encoding);
        String err = IOUtils.readFully(result.getErrorStream()).toString(encoding);
        if(err == null || err.isEmpty()) err = result.getExitErrorMessage();
        return command.process(out, err == null || err.isEmpty() ? null : new SSHException(err));
    }

    /**
     * Executes the specified action in the channel context.
     *
     * @param command The command to apply in channel context.
     * @return The execution result.
     * @throws java.io.IOException Some I/O error occurs in the channel.
     * @throws E                   Non-I/O exception raised by the command.
     */
    @Override
    public synchronized <I, O, E extends Exception> O exec(final ChannelProcessor<I, O, E> command,
                                                           final I input) throws IOException, E {
        if (isConnected())
            switch (mode) {
                case CONNECTION_PER_EXECUTION:
                    try (final Session s = startSession()) {
                        return exec(s, encoding, channelParams, command, input);
                    }
                case SINGLETON_CONNECTION:
                    if (session == null) { //starts a new session
                        session = startSession();
                        return exec(command, input);
                    } else if (session.isOpen()) {
                        return exec(session, encoding, channelParams, command, input);
                    } else {//close dead session and initialize a new session
                        session.close();
                        session = null;
                        return exec(command, input);
                    }
                default:
                    throw new IOException(String.format("Unsupported channel mode %s.", mode));
            }
        else if (localHost == null) {
            connect(remoteHost, remotePort);
            if (auth != null)
                auth.authenticate(this);
            return exec(command, input);
        } else {
            connect(remoteHost, remotePort, localHost, localPort);
            if (auth != null)
                auth.authenticate(this);
            return exec(command, input);
        }
    }
}
