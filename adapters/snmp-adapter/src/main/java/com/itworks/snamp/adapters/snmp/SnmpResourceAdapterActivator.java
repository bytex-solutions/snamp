package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.licensing.LicensingException;
import org.osgi.service.jndi.JNDIContextManager;
import org.snmp4j.mp.MPv3;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpResourceAdapterActivator extends AbstractResourceAdapterActivator<SnmpResourceAdapter> {
    private static final class SnmpAdapterConfigurationEntityDescriptionManager extends ConfigurationEntityDescriptionManager<SnmpAdapterConfigurationDescriptor> {

        /**
         * Creates a new instance of the configuration description provider.
         *
         * @param dependencies A collection of provider dependencies.
         * @return A new instance of the configuration description provider.
         * @throws Exception An exception occurred during provider instantiation.
         */
        @Override
        public SnmpAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new SnmpAdapterConfigurationDescriptor();
        }
    }
    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     */
    public SnmpResourceAdapterActivator() {
        super(SnmpResourceAdapter.NAME, new SnmpAdapterConfigurationEntityDescriptionManager(),
                new LicensingDescriptionServiceManager<>(SnmpAdapterLimitations.class, SnmpAdapterLimitations.fallbackFactory));
    }

    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(SnmpAdapterLimitations.licenseReader);
        dependencies.add(new SimpleDependency<>(JNDIContextManager.class));
    }

    private static DirContextFactory toFactory(final JNDIContextManager contextManager){
        return new DirContextFactory() {
            @Override
            public DirContext create(final Hashtable<?, ?> env) throws NamingException {
                return contextManager.newInitialDirContext(env);
            }
        };
    }

    @Override
    protected SnmpResourceAdapter createAdapter(final String adapterInstanceName,
                                                final Map<String, String> parameters,
                                                final Map<String, ManagedResourceConfiguration> resources,
                                                final RequiredService<?>... dependencies) throws IOException{
        try{
            SnmpAdapterLimitations.current().verifyServiceVersion(SnmpResourceAdapter.class);
            final String port = parameters.containsKey(PORT_PARAM_NAME) ? parameters.get(PORT_PARAM_NAME) : "161";
            final String address = parameters.containsKey(HOST_PARAM_NAME) ? parameters.get(HOST_PARAM_NAME) : "127.0.0.1";
            final String socketTimeout = parameters.containsKey(SOCKET_TIMEOUT_PARAM) ? parameters.get(SOCKET_TIMEOUT_PARAM) : "0";
            if(parameters.containsKey(SNMPv3_GROUPS_PARAM) || parameters.containsKey(LDAP_GROUPS_PARAM)){
                SnmpAdapterLimitations.current().verifyAuthenticationFeature();
                final JNDIContextManager contextManager = getDependency(RequiredServiceAccessor.class, JNDIContextManager.class, dependencies);
                final SecurityConfiguration security = new SecurityConfiguration(MPv3.createLocalEngineID(), toFactory(contextManager));
                security.read(parameters);
                return new SnmpResourceAdapter(Integer.valueOf(port), address, security, Integer.valueOf(socketTimeout), new SnmpThreadPoolConfig(parameters, adapterInstanceName), resources);
            }
            else return new SnmpResourceAdapter(Integer.valueOf(port), address, null, Integer.valueOf(socketTimeout), new SnmpThreadPoolConfig(parameters, adapterInstanceName), resources);
        }
        catch (final LicensingException e){
            SnmpHelpers.log(Level.SEVERE, "Unable to instatiate SNMP adapter due its license limitations.", e);
            return null;
        }
    }
}
