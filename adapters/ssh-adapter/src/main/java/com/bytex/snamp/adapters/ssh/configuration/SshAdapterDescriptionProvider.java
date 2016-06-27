package com.bytex.snamp.adapters.ssh.configuration;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.adapters.ResourceAdapterDescriptionProvider;
import com.bytex.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.bytex.snamp.concurrent.LazyContainers;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import net.schmizz.sshj.userauth.keyprovider.*;
import org.apache.sshd.common.KeyPairProvider;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration.THREAD_POOL_KEY;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class SshAdapterDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements ResourceAdapterDescriptionProvider {
    private static final String HOST_PARAM = "host";
    private static final String PORT_PARAM = "port";
    private static final String HOST_KEY_FILE_PARAM = "hostKeyFile";
    private static final String HOST_KEY_FORMAT_PARAM = "hostKeyFormat";
    private static final String JAAS_DOMAIN_PARAM = "jaasDomain";
    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";

    private static final String PUBLIC_KEY_FILE_PARAM = "publicKeyFile";
    private static final String PUBLIC_KEY_FILE_FORMAT_PARAM = "publicKeyFileFormat";

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 22;
    private static final String DEFAULT_HOST_KEY = "ssh-adapter.ser";

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
                    result.addAll(nics.nextElement().getInterfaceAddresses().stream()
                            .map(iface -> iface.getAddress().getHostAddress())
                            .collect(Collectors.toCollection(LinkedList::new)));
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
                    THREAD_POOL_KEY,
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

    private static final LazyValue<SshAdapterDescriptionProvider> INSTANCE = LazyContainers.NORMAL.create(SshAdapterDescriptionProvider::new);

    private SshAdapterDescriptionProvider() {
        super(new AdapterConfigurationInfo());
    }

    public static SshAdapterDescriptionProvider getInstance(){
        return INSTANCE.get();
    }

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
