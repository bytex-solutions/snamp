package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration;
import net.schmizz.sshj.userauth.keyprovider.*;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.ssh.SshAdapterConfigurationDescriptor.*;

/**
 * Represents OSGi activator for {@link com.itworks.snamp.adapters.ssh.SshAdapter} resource adapter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SshAdapterActivator extends AbstractResourceAdapterActivator<SshAdapter> {


    public SshAdapterActivator() {
        super(SshAdapter.NAME, SshHelpers.getLogger());
    }

    /**
     * Initializes a new instance of the resource adapter.
     *
     * @param parameters   A collection of initialization parameters.
     * @param resources    A collection of managed resources to be exposed via adapter.
     * @param dependencies A collection of dependencies used by adapter.
     * @return A new instance of the adapter.
     * @throws Exception Unable to instantiate resource adapter.
     */
    @Override
    protected SshAdapter createAdapter(final String adapterInstanceName,
                                        final Map<String, String> parameters,
                                       final Map<String, AgentConfiguration.ManagedResourceConfiguration> resources, final RequiredService<?>... dependencies) throws Exception {
        final String host = parameters.containsKey(HOST_PARAM) ?
                parameters.get(HOST_PARAM) :
                DEFAULT_HOST;
        final int port = parameters.containsKey(PORT_PARAM) ?
                Integer.parseInt(parameters.get(PORT_PARAM)) :
                DEFAULT_PORT;
        final String certificateFile = parameters.containsKey(CERTIFICATE_FILE_PARAM) ?
                parameters.get(CERTIFICATE_FILE_PARAM) :
                DEFAULT_CERTIFICATE;
        return new SshAdapter(host,
                port,
                certificateFile,
                createSecuritySettings(parameters, getLogger()),
                resources);
    }

    private static SshSecuritySettings createSecuritySettings(final Map<String, String> parameters, final Logger logger){
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
            public PublicKey getClientPublicKey() {
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
                    logger.log(Level.WARNING, "Invalid SSH public key file.", e);
                }
                return null;
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
}
