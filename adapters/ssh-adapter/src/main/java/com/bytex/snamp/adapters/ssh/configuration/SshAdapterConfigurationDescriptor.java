package com.bytex.snamp.adapters.ssh.configuration;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.*;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration.THREAD_POOL_KEY;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class SshAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    static final String HOST_PARAM = "host";
    static final String PORT_PARAM = "port";
    static final String HOST_KEY_FILE_PARAM = "hostKeyFile";
    static final String HOST_KEY_FORMAT_PARAM = "hostKeyFormat";
    static final String JAAS_DOMAIN_PARAM = "jaasDomain";
    static final String USER_NAME_PARAM = "userName";
    static final String PASSWORD_PARAM = "password";

    static final String PUBLIC_KEY_FILE_PARAM = "publicKeyFile";
    static final String PUBLIC_KEY_FILE_FORMAT_PARAM = "publicKeyFileFormat";

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

    public SshAdapterConfigurationDescriptor() {
        super(new AdapterConfigurationInfo());
    }
}
