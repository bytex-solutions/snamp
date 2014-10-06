package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration;

import java.util.Map;

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
    protected SshAdapter createAdapter(final Map<String, String> parameters, final Map<String, AgentConfiguration.ManagedResourceConfiguration> resources, final RequiredService<?>... dependencies) throws Exception {
        final String host = parameters.containsKey(HOST_PARAM) ?
                parameters.get(HOST_PARAM) :
                DEFAULT_HOST;
        final int port = parameters.containsKey(PORT_PARAM) ?
                Integer.parseInt(parameters.get(PORT_PARAM)) :
                DEFAULT_PORT;
        final String certificateFile = parameters.containsKey(CERTIFICATE_FILE_PARAM) ?
                parameters.get(CERTIFICATE_FILE_PARAM) :
                DEFAULT_CERTIFICATE;
        return new SshAdapter(host, port, certificateFile, createSecuritySettings(parameters), resources);
    }

    private static SshSecuritySettings createSecuritySettings(final Map<String, String> parameters){
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
        };
    }
}
