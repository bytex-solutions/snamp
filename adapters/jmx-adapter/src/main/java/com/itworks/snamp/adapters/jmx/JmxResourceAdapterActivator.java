package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;

import javax.management.ObjectName;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;

import static com.itworks.snamp.adapters.jmx.JmxAdapterConfigurationProvider.*;

/**
 * Represents JMX resource adapter activator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxResourceAdapterActivator extends AbstractResourceAdapterActivator<JmxResourceAdapter> {
    private static final class JmxConfigurationDescriptor extends ConfigurationEntityDescriptionManager<JmxAdapterConfigurationProvider> {

        @Override
        protected JmxAdapterConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new JmxAdapterConfigurationProvider();
        }
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     */
    public JmxResourceAdapterActivator() {
        super(JmxResourceAdapter.NAME,
                new JmxConfigurationDescriptor());
    }

    /**
     * Exposes additional adapter dependencies.
     * <p>
     * In the default implementation this method does nothing.
     * </p>
     *
     * @param dependencies A collection of dependencies to fill.
     */
    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(JmxAdapterLicenseLimitations.licenseReader);
    }

    /**
     * Initializes a new instance of the resource adapter.
     *
     * @param adapterInstanceName The name of the adapter instance.
     * @param parameters   A collection of initialization parameters.
     * @param dependencies A collection of dependencies used by adapter.
     * @return A new instance of the adapter.
     * @throws Exception Unable to instantiate resource adapter.
     */
    @Override
    protected JmxResourceAdapter createAdapter(final String adapterInstanceName,
                                               final Map<String, String> parameters,
                                               final RequiredService<?>... dependencies) throws Exception {
        if (parameters.containsKey(OBJECT_NAME_PARAM)) {
            JmxAdapterLicenseLimitations.current().verifyServiceVersion(JmxResourceAdapter.class);
            final JmxResourceAdapter adapter = new JmxResourceAdapter(adapterInstanceName, new ObjectName(parameters.get(OBJECT_NAME_PARAM)),
                    parameters.containsKey(USE_PLATFORM_MBEAN_PARAM) && Boolean.valueOf(parameters.get(USE_PLATFORM_MBEAN_PARAM)));
            if (parameters.containsKey(DEBUG_USE_PURE_SERIALIZATION_PARAM) && Boolean.valueOf(parameters.get(DEBUG_USE_PURE_SERIALIZATION_PARAM)))
                adapter.usePureSerialization();
            return adapter;
        }
        else throw new MalformedURLException(String.format("Adapter configuration has no %s entry", OBJECT_NAME_PARAM));
    }


}
