package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import org.snmp4j.mp.MPv3;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpResourceAdapterActivator extends AbstractResourceAdapterActivator<SnmpResourceAdapter> {
    public static final String ADAPTER_NAME = "snmp";

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     */
    protected SnmpResourceAdapterActivator() {
        super(ADAPTER_NAME, SnmpHelpers.getLogger());
    }

    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(SnmpAdapterLimitations.licenseReader);
    }

    @Override
    protected SnmpResourceAdapter createAdapter(final Map<String, String> parameters, final Map<String, ManagedResourceConfiguration> resources) throws IOException{
        final String port = parameters.containsKey(PORT_PARAM_NAME) ? parameters.get(PORT_PARAM_NAME) : "161";
        final String address = parameters.containsKey(HOST_PARAM_NAME) ? parameters.get(HOST_PARAM_NAME) : "127.0.0.1";
        final String socketTimeout = parameters.containsKey(SOCKET_TIMEOUT_PARAM) ? parameters.get(SOCKET_TIMEOUT_PARAM) : "0";
        if(parameters.containsKey(SNMPv3_GROUPS_PARAM) || parameters.containsKey(LDAP_GROUPS_PARAM)){
            SnmpAdapterLimitations.current().verifyAuthenticationFeature();
            final SecurityConfiguration security = new SecurityConfiguration(MPv3.createLocalEngineID());
            security.read(parameters);
            return new SnmpResourceAdapter(Integer.valueOf(port), address, security, Integer.valueOf(socketTimeout), resources);
        }
        else return new SnmpResourceAdapter(Integer.valueOf(port), address, null, Integer.valueOf(socketTimeout), resources);
    }
}
