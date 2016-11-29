package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.gateway.GatewayDescriptionProvider;
import com.bytex.snamp.gateway.SelectableGatewayParameterDescriptor;
import net.schmizz.sshj.userauth.keyprovider.*;
import org.apache.sshd.common.KeyPairProvider;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.getValueAsInt;
import static com.bytex.snamp.configuration.GatewayConfiguration.THREAD_POOL_KEY;
import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SshGatewayDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements GatewayDescriptionProvider {
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
    private static final String DEFAULT_HOST_KEY = "ssh-gateway.ser";

    private static final class GatewayConfigurationInfo extends ResourceBasedConfigurationEntityDescription<GatewayConfiguration> {
        private static final String RESOURCE_NAME = "SshGatewayConfig";

        private final class HostParameter extends ParameterDescriptionImpl implements SelectableGatewayParameterDescriptor {
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
                            .collect(Collectors.toList()));
                return result.stream().toArray(String[]::new);
            }
        }

        private GatewayConfigurationInfo() {
            super(RESOURCE_NAME,
                    GatewayConfiguration.class,
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

    private static final LazySoftReference<SshGatewayDescriptionProvider> INSTANCE = new LazySoftReference<>();

    private SshGatewayDescriptionProvider() {
        super(new GatewayConfigurationInfo());
    }

    static SshGatewayDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(SshGatewayDescriptionProvider::new);
    }

    String getHost(final Map<String, String> parameters){
        return getValue(parameters, HOST_PARAM, Function.identity(), () -> DEFAULT_HOST);
    }

    int getPort(final Map<String, String> parameters) {
        return getValueAsInt(parameters, PORT_PARAM, Integer::parseInt, () -> DEFAULT_PORT);
    }

    KeyPairProvider getKeyPairProvider(final Map<String, String> parameters) {
        final KeyPairProviderFactory factory = getValue(parameters, HOST_KEY_FORMAT_PARAM, KeyPairProviderFactory::parse, () -> KeyPairProviderFactory.JAVA_KEY);
        final String hostKeyFile = getValue(parameters, HOST_KEY_FILE_PARAM, Function.identity(), () -> DEFAULT_HOST_KEY);
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
                return callAndWrapException(() -> {
                    KeyFormat format = getClientPublicKeyFormat();
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
                }, InvalidKeyException::new);
            }

            @Override
            public KeyFormat getClientPublicKeyFormat() {
                return getValue(parameters, PUBLIC_KEY_FILE_FORMAT_PARAM, format -> {
                    switch (format.toLowerCase()) {
                        case "pkcs8":
                            return KeyFormat.PKCS8;
                        case "openssh":
                            return KeyFormat.OpenSSH;
                        case "putty":
                            return KeyFormat.PuTTY;
                        default:
                            return KeyFormat.Unknown;
                    }
                }, () -> KeyFormat.Unknown);
            }
        };
    }

    SshSecuritySettings getSecuritySettings(final Map<String, String> parameters){
        return getSecuritySettingsImpl(parameters);
    }
}
