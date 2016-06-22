package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Box;
import com.bytex.snamp.Consumer;
import com.bytex.snamp.SafeConsumer;
import com.bytex.snamp.configuration.internal.CMResourceAdapterParser;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Maps;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.bytex.snamp.configuration.impl.SerializableAgentConfiguration.SerializableResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMResourceAdapterParserImpl implements CMResourceAdapterParser {
    private static final String ADAPTER_PID_TEMPLATE = "com.bytex.snamp.adapters.%s";
    private static final String ADAPTER_INSTANCE_NAME_PROPERTY = "$adapterInstanceName$";
    private static final String ALL_ADAPTERS_QUERY = String.format("(%s=%s)", Constants.SERVICE_PID, String.format(ADAPTER_PID_TEMPLATE, "*"));

    private static final class ResourceAdapterConfigurationException extends PersistentConfigurationException{
        private static final long serialVersionUID = -242953184038600223L;

        private ResourceAdapterConfigurationException(final String pid, final Throwable e) {
            super(pid, SerializableResourceAdapterConfiguration.class, e);
        }
    }

    /**
     * Returns name of the resource adapter instance by its persistent identifier.
     * @param factoryPID Resource adapter persistent identifier.
     * @return The name of the resource adapter.
     */
    private static String getAdapterType(final String factoryPID){
        return factoryPID.replaceFirst(String.format(ADAPTER_PID_TEMPLATE, ""), "");
    }

    @Override
    public String getAdapterFactoryPersistentID(final String adapterType) {
        return String.format(ADAPTER_PID_TEMPLATE, adapterType);
    }

    @Override
    public String getAdapterInstanceName(final Dictionary<String, ?> adapterConfig) {
        return Utils.getProperty(adapterConfig, ADAPTER_INSTANCE_NAME_PROPERTY, String.class, "");
    }

    private static void fillAdapterParameters(final Dictionary<String, ?> adapterConfig,
                                              final Map<String, String> output){
        final Enumeration<String> names = adapterConfig.keys();
        while (names.hasMoreElements()){
            final String name = names.nextElement();
            switch (name){
                default:
                    final Object value = adapterConfig.get(name);
                    if(value != null)
                        output.put(name, value.toString());
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
                case ADAPTER_INSTANCE_NAME_PROPERTY:
            }
        }
    }

    @Override
    public Map<String, String> getAdapterParameters(final Dictionary<String, ?> adapterConfig) {
        final Map<String, String> result = Maps.newHashMapWithExpectedSize(adapterConfig.size());
        fillAdapterParameters(adapterConfig, result);
        return result;
    }

    private static <E extends Exception> void forEachAdapter(final ConfigurationAdmin admin,
                                                             final String filter,
                                                             final Consumer<Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        final Configuration[] configs = admin.listConfigurations(filter);
        if(configs != null && configs.length > 0)
            for(final Configuration config: configs)
                reader.accept(config);
    }

    void readAdapters(final ConfigurationAdmin admin,
                                     final Map<String, SerializableResourceAdapterConfiguration> output) throws IOException {
        try {
            forEachAdapter(admin, ALL_ADAPTERS_QUERY, (SafeConsumer<Configuration>) config -> {
                final String adapterInstanceName = getAdapterInstanceName(config.getProperties());
                final SerializableResourceAdapterConfiguration adapter = parse(config);
                output.put(adapterInstanceName, adapter);
            });
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    void removeAll(final ConfigurationAdmin admin) throws IOException {
        try {
            forEachAdapter(admin, ALL_ADAPTERS_QUERY, Configuration::delete);
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public SerializableResourceAdapterConfiguration parse(final Configuration config) {
        final SerializableResourceAdapterConfiguration result = new SerializableResourceAdapterConfiguration();
        result.setAdapterName(getAdapterType(config.getFactoryPid()));
        //deserialize parameters
        fillAdapterParameters(config.getProperties(), result.getParameters());
        result.reset();
        return result;
    }

    private static void serialize(final String adapterInstanceName,
                                  final SerializableResourceAdapterConfiguration adapter,
                                  final Configuration output) throws IOException{
        serialize(adapter, output);
        Utils.setProperty(output.getProperties(), ADAPTER_INSTANCE_NAME_PROPERTY, adapterInstanceName);
    }

    private static void serialize(final SerializableResourceAdapterConfiguration adapter,
                                  final Configuration output) throws IOException{
        final Dictionary<String, String> result = new Hashtable<>(4);

        for(final Map.Entry<String, String> entry: adapter.getParameters().entrySet())
            switch (entry.getKey()) {
                default: result.put(entry.getKey(), entry.getValue());
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
            }
        output.update(result);
    }

    @Override
    public void serialize(final ResourceAdapterConfiguration input, final Configuration output) throws IOException {
        assert input instanceof SerializableResourceAdapterConfiguration;
        serialize((SerializableResourceAdapterConfiguration) input, output);
    }

    private void serialize(final String adapterInstance,
                             final SerializableResourceAdapterConfiguration adapter,
                             final ConfigurationAdmin admin) throws ResourceAdapterConfigurationException {
        try {
            //find existing configuration of adapters
            final Box<Boolean> updated = new Box<>(Boolean.FALSE);
            forEachAdapter(admin, String.format("(%s=%s)", ADAPTER_INSTANCE_NAME_PROPERTY, adapterInstance), config -> {
                serialize(adapterInstance, adapter, config);
                updated.set(Boolean.TRUE);
            });
            //no existing configuration, creates a new configuration
            if (!updated.get())
                serialize(adapterInstance,
                        adapter,
                        admin.createFactoryConfiguration(getAdapterFactoryPersistentID(adapter.getAdapterName()), null));
        } catch (final IOException | InvalidSyntaxException e) {
            throw new ResourceAdapterConfigurationException(adapterInstance, e);
        }
    }

    void saveChanges(final SerializableAgentConfiguration config,
              final ConfigurationAdmin admin) throws IOException {
        //remove all unnecessary adapters
        try {
            final Map<String, SerializableResourceAdapterConfiguration> adapters = config.adapters;
            forEachAdapter(admin, ALL_ADAPTERS_QUERY, output -> {
                final String adapterInstance = getAdapterInstanceName(output.getProperties());
                if (!adapters.containsKey(adapterInstance))
                    output.delete();
            });
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
        //save each modified adapter
        config.modifiedAdapters((adapterInstance, adapter) -> {
            if (adapter instanceof SerializableResourceAdapterConfiguration && ((SerializableResourceAdapterConfiguration) adapter).isModified())
                serialize(adapterInstance, (SerializableResourceAdapterConfiguration)adapter, admin);
            return true;
        });
    }
}
