package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import net.schmizz.sshj.userauth.keyprovider.*;
import org.apache.sshd.common.KeyPairProvider;

import java.io.File;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.security.PublicKey;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    static final String HOST_PARAM = "host";
    static final String DEFAULT_HOST = "localhost";

    static final String PORT_PARAM = "port";
    static final int DEFAULT_PORT = 22;

    private static final String HOST_KEY_FILE_PARAM = "hostKeyFile";
    private static final String DEFAULT_HOST_KEY = "ssh-adapter.ser";

    private static final String HOST_KEY_FORMAT_PARAM = "hostKeyFormat";


    private static final String JAAS_DOMAIN_PARAM = "jaasDomain";
    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";

    private static final String PUBLIC_KEY_FILE_PARAM = "publicKeyFile";
    private static final String PUBLIC_KEY_FILE_FORMAT_PARAM = "publicKeyFileFormat";

    private static final class AdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration> {
        private static final String RESOURCE_NAME = "SshAdapterConfig";

        private final class HostParameter extends ParameterDescriptionImpl implements SelectableAdapterParameterDescriptor {
            private HostParameter() {
                super(HOST_PARAM);
            }

            @Override
            public String[] suggestValues(final Map<String, String> connectionOptions, final Locale loc) throws Exception {
                final Set<String> result = new HashSet<>(10);
                final Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                while (nics.hasMoreElements())
                    for (final InterfaceAddress iface : nics.nextElement().getInterfaceAddresses())
                        result.add(iface.getAddress().getHostAddress());
                return ArrayUtils.toArray(result, String.class);
            }
        }

        private AdapterConfigurationInfo() {
            super(RESOURCE_NAME,
                    ResourceAdapterConfiguration.class,
                    HOST_PARAM,
                    PORT_PARAM,
                    HOST_KEY_FILE_PARAM,
                    HOST_KEY_FORMAT_PARAM,
                    JAAS_DOMAIN_PARAM,
                    PASSWORD_PARAM,
                    PUBLIC_KEY_FILE_FORMAT_PARAM,
                    PUBLIC_KEY_FILE_PARAM);
        }

        @Override
        protected ParameterDescriptionImpl createParameterDescriptor(final String parameterName) {
            switch (parameterName){
                case HOST_PARAM: return new HostParameter();
                default: return super.createParameterDescriptor(parameterName);
            }
        }
    }

    SshAdapterConfigurationDescriptor() {
        super(new AdapterConfigurationInfo());
    }

    static KeyPairProvider createKeyPairProvider(final Map<String, String> parameters){
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

    static SshSecuritySettings createSecuritySettings(final Map<String, String> parameters){
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
                    SshHelpers.log(Level.WARNING, "Invalid SSH public key file.", e);
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
