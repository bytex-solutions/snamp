package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.*;

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

    static final String CERTIFICATE_FILE_PARAM = "certificateFile";
    static final String DEFAULT_CERTIFICATE = "ssh-adapter.ser";

    static final String JAAS_DOMAIN_PARAM = "jaasDomain";
    static final String USER_NAME_PARAM = "userName";
    static final String PASSWORD_PARAM = "password";

    static final String PUBLIC_KEY_FILE_PARAM = "publicKeyFile";
    static final String PUBLIC_KEY_FILE_FORMAT_PARAM = "publicKeyFileFormat";

    /**
     * Represents attribute boolean option applied to tabular attribute.
     */
    static final String COLUMN_BASED_OUTPUT_PARAM = "columnBasedPrint";

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
            super(ResourceAdapterConfiguration.class,
                    HOST_PARAM,
                    PORT_PARAM,
                    CERTIFICATE_FILE_PARAM,
                    JAAS_DOMAIN_PARAM,
                    PASSWORD_PARAM,
                    PUBLIC_KEY_FILE_FORMAT_PARAM,
                    PUBLIC_KEY_FILE_PARAM);
        }

        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
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
