package com.bytex.snamp.adapters.ssh.configuration;

import com.bytex.snamp.adapters.ResourceAdapterConfigurationParser;
import net.schmizz.sshj.userauth.keyprovider.*;
import org.apache.sshd.common.KeyPairProvider;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.Map;

import static com.bytex.snamp.adapters.ssh.configuration.SshAdapterConfigurationDescriptor.*;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public final class SshAdapterConfigurationParser extends ResourceAdapterConfigurationParser {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 22;
    private static final String DEFAULT_HOST_KEY = "ssh-adapter.ser";

    public String getHost(final Map<String, String> parameters){
        return parameters.containsKey(HOST_PARAM) ?
                parameters.get(HOST_PARAM) :
                DEFAULT_HOST;
    }

    public int getPort(final Map<String, String> parameters) {
        return parameters.containsKey(PORT_PARAM) ?
                Integer.parseInt(parameters.get(PORT_PARAM)) :
                DEFAULT_PORT;
    }

    public KeyPairProvider getKeyPairProvider(final Map<String, String> parameters){
        final KeyPairProviderFactory factory;
        switch (parameters.containsKey(HOST_KEY_FORMAT_PARAM) ?
                parameters.get(HOST_KEY_FORMAT_PARAM) :
                ""){
            case "pem":
            case "PEM":
                factory = KeyPairProviderFactory.PEM_KEY;
                break;
            default:
                factory = KeyPairProviderFactory.JAVA_KEY;
                break;
        }

        final String hostKeyFile = parameters.containsKey(HOST_KEY_FILE_PARAM) ?
                parameters.get(HOST_KEY_FILE_PARAM) :
                DEFAULT_HOST_KEY;
        return factory.loadPair(hostKeyFile);
    }

    private static SshSecuritySettings getSecuritySettingsImpl(final Map<String, String> parameters){
        return new SshSecuritySettings() {

            @Override
            public String getUserName() {
                return parameters.get(USER_NAME_PARAM);
            }

            @Override
            public String getPassword() {
                return parameters.get(PASSWORD_PARAM);
            }

            @Override
            public boolean hasUserCredentials() {
                return parameters.containsKey(USER_NAME_PARAM) && parameters.containsKey(PASSWORD_PARAM);
            }

            @Override
            public String getJaasDomain() {
                return parameters.get(JAAS_DOMAIN_PARAM);
            }

            @Override
            public boolean hasJaasDomain() {
                return parameters.containsKey(JAAS_DOMAIN_PARAM);
            }

            @Override
            public boolean hasClientPublicKey() {
                return parameters.containsKey(PUBLIC_KEY_FILE_PARAM);
            }

            @Override
            public PublicKey getClientPublicKey() throws InvalidKeyException {
                final File keyFile = new File(parameters.get(PUBLIC_KEY_FILE_PARAM));
                KeyFormat format = getClientPublicKeyFormat();
                try {
                    if (format == KeyFormat.Unknown)
                        format = KeyProviderUtil.detectKeyFileFormat(keyFile);
                    final FileKeyProvider provider;
                    switch (format) {
                        case PKCS8:
                            provider = new PKCS8KeyFile();
                            break;
                        case OpenSSH:
                            provider = new OpenSSHKeyFile();
                            break;
                        case PuTTY:
                            provider = new PuTTYKeyFile();
                            break;
                        default:
                            throw new IOException("Unknown public key format.");
                    }
                    provider.init(keyFile);
                    return provider.getPublic();
                } catch (final IOException e) {
                    throw new InvalidKeyException(e);
                }
            }

            @Override
            public KeyFormat getClientPublicKeyFormat() {
                if (parameters.containsKey(PUBLIC_KEY_FILE_FORMAT_PARAM))
                    switch (parameters.get(PUBLIC_KEY_FILE_FORMAT_PARAM).toLowerCase()) {
                        case "pkcs8":
                            return KeyFormat.PKCS8;
                        case "openssh":
                            return KeyFormat.OpenSSH;
                        case "putty":
                            return KeyFormat.PuTTY;
                    }
                return KeyFormat.Unknown;
            }
        };
    }

    public SshSecuritySettings getSecuritySettings(final Map<String, String> parameters){
        return getSecuritySettingsImpl(parameters);
    }
}
