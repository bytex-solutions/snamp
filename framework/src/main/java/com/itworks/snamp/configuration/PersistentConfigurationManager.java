package com.itworks.snamp.configuration;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.*;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.internal.RecordReader;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.*;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration;
import static com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableResourceAdapterConfiguration;

/**
 * Represents SNAMP configuration manager that uses {@link org.osgi.service.cm.ConfigurationAdmin}
 * to store and read SNAMP configuration.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe
public final class PersistentConfigurationManager extends AbstractAggregator implements ConfigurationManager {
    private static final TypeToken<Map<String, AttributeConfiguration>> ATTRS_MAP_TYPE = new TypeToken<Map<String, AttributeConfiguration>>() {};
    private static final TypeToken<Map<String, EventConfiguration>> EVENTS_MAP_TYPE = new TypeToken<Map<String, EventConfiguration>>() {};

    private static final String ADAPTER_PID_TEMPLATE = "com.itworks.snamp.adapters.%s";
    private static final String ADAPTER_INSTANCE_NAME_PROPERTY = "$adapterInstanceName$";
    private static final String ALL_ADAPTERS_QUERY = String.format("(%s=%s)", Constants.SERVICE_PID, String.format(ADAPTER_PID_TEMPLATE, "*"));

    private static final String CONNECTOR_PID_TEMPLATE = "com.itworks.snamp.connectors.%s";
    private static final String RESOURCE_NAME_PROPERTY = "$resourceName$";
    private static final String ALL_CONNECTORS_QUERY = String.format("(%s=%s)", Constants.SERVICE_PID, String.format(CONNECTOR_PID_TEMPLATE, "*"));
    private static final String CONNECTION_STRING_PROPERTY = "$connectionString$";
    private static final String ATTRIBUTES_PROPERTY = "$attributes$";
    private static final String EVENTS_PROPERTY = "$events$";

    private static final Consumer<Configuration, IOException> clearAllConsumer = new Consumer<Configuration, IOException>() {
        @Override
        public void accept(final Configuration config) throws IOException {
            config.delete();
        }
    };

    /**
     * Represents an exception happens when persistent configuration manager cannot
     * restore SNAMP configuration entity from OSGi persistent configuration store.
     * This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class PersistentConfigurationException extends IOException{
        private static final long serialVersionUID = -518115699501252969L;

        /**
         * Represents PID that references the invalid configuration.
         */
        public final String persistenceID;
        /**
         * Represents type of the configuration entity that cannot be restored from storage.
         */
        public final Class<? extends ConfigurationEntity> entityType;

        private PersistentConfigurationException(final String pid,
                                                final Class<? extends ConfigurationEntity> entityType,
                                                final Throwable e){
            super(String.format("Unable to read SNAMP %s configuration", pid), e);
            this.persistenceID = pid;
            this.entityType = entityType;
        }
    }

    private static final class ConfigurationEntry<V extends ConfigurationEntity> implements Map.Entry<String, V>{
        private final String entryName;
        private final V entry;

        private ConfigurationEntry(final String name, final V content){
            entryName = name;
            entry = content;
        }

        @Override
        public String getKey() {
            return entryName;
        }

        @Override
        public V getValue() {
            return entry;
        }

        @Override
        public V setValue(final V value) {
            throw new UnsupportedOperationException();
        }
    }


    private final ConfigurationAdmin admin;
    private SerializableAgentConfiguration configuration;
    private final Logger logger;

    /**
     * Initializes a new configuration manager.
     * @param configAdmin OSGi configuration admin. Cannot be {@literal null}.
     */
    public PersistentConfigurationManager(final ConfigurationAdmin configAdmin){
        admin = Objects.requireNonNull(configAdmin, "configAdmin is null.");
        logger = Logger.getLogger(getClass().getName());
    }

    /**
     * Initializes a new configuration manager.
     * @param configAdmin A reference to {@link org.osgi.service.cm.ConfigurationAdmin} service.
     */
    public PersistentConfigurationManager(final ServiceReferenceHolder<ConfigurationAdmin> configAdmin) {
        this(configAdmin.getService());
    }

    /**
     * Returns persistent identifier of the specified resource adapter.
     * @param adapterType The name of the adapter instance.
     * @return Persistent identifier.
     */
    public static String getAdapterFactoryPersistentID(final String adapterType){
        return String.format(ADAPTER_PID_TEMPLATE, adapterType);
    }

    /**
     * Returns managed connector persistent identifier.
     * @param connectorType The type of the managed resource connector.
     * @return The persistent identifier.
     */
    public static String getConnectorFactoryPersistentID(final String connectorType){
        return String.format(CONNECTOR_PID_TEMPLATE, connectorType);
    }

    /**
     * Returns name of the resource adapter instance by its persistent identifier.
     * @param factoryPID Resource adapter persistent identifier.
     * @return The name of the resource adapter.
     */
    private static String getAdapterType(final String factoryPID){
        return factoryPID.replaceFirst(String.format(ADAPTER_PID_TEMPLATE, ""), "");
    }

    /**
     * Returns managed resource name by its persistent identifier.
     * @param factoryPID Managed resource persistent identifier.
     * @return The name of the managed resource.
     */
    private static String getConnectorType(final String factoryPID){
        return factoryPID.replaceFirst(String.format(CONNECTOR_PID_TEMPLATE, ""), "");
    }

    /**
     * Extracts the name of the adapter instance from its configuration.
     * @param adapterConfig The adapter instance configuration supplied by {@link org.osgi.service.cm.Configuration} object.
     * @return Adapter instance name.
     */
    public static String getAdapterInstanceName(final Dictionary<String, ?> adapterConfig){
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
                case ADAPTER_INSTANCE_NAME_PROPERTY:
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
            }
        }
    }

    public static Map<String, String> getAdapterParameters(final Dictionary<String, ?> adapterConfig){
        final Map<String, String> result = Maps.newHashMapWithExpectedSize(adapterConfig.size());
        fillAdapterParameters(adapterConfig, result);
        return result;
    }

    private static ConfigurationEntry<ResourceAdapterConfiguration> readAdapterConfiguration(
            final String factoryPID,
            final Dictionary<String, ?> config) {
        final SerializableResourceAdapterConfiguration result = new SerializableResourceAdapterConfiguration();
        result.setAdapterName(getAdapterType(factoryPID));
        //deserialize parameters
        fillAdapterParameters(config, result.getParameters());
        result.reset();
        return new ConfigurationEntry<ResourceAdapterConfiguration>(getAdapterInstanceName(config), result);
    }

    private static ConfigurationEntry<ResourceAdapterConfiguration> readAdapterConfiguration(final Configuration config) {
        return readAdapterConfiguration(config.getFactoryPid(), config.getProperties());
    }

    private static <E extends Exception> void forEachAdapter(final ConfigurationAdmin admin,
                                                             final String filter,
                                                             final Consumer<Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        final Configuration[] configs = admin.listConfigurations(filter);
        if(configs != null && configs.length > 0)
            for(final Configuration config: configs)
                reader.accept(config);
    }

    private static <E extends Exception> void forEachAdapter(final ConfigurationAdmin admin,
                                                             final Consumer<Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        forEachAdapter(admin, ALL_ADAPTERS_QUERY, reader);
    }

    private static void readAdapters(final ConfigurationAdmin admin,
                                     final Map<String, ResourceAdapterConfiguration> output) throws IOException, InvalidSyntaxException {
        forEachAdapter(admin, new Consumer<Configuration, IOException>() {
            @Override
            public void accept(final Configuration config) throws IOException {
                final ConfigurationEntry<ResourceAdapterConfiguration> entry = readAdapterConfiguration(config);
                output.put(entry.getKey(), entry.getValue());
            }
        });
    }

    /**
     * Extracts resource connection string from the managed resource configuration.
     * @param resourceConfig A dictionary that represents managed resource configuration.
     * @return Resource connection string.
     */
    public static String getConnectionString(final Dictionary<String, ?> resourceConfig){
        return Utils.getProperty(resourceConfig, CONNECTION_STRING_PROPERTY, String.class, "");
    }

    /**
     * Extracts resource name from the managed resource configuration.
     * @param resourceConfig A dictionary that represents managed resource configuration.
     * @return The resource name.
     */
    public static String getResourceName(final Dictionary<String, ?> resourceConfig){
        return Utils.getProperty(resourceConfig, RESOURCE_NAME_PROPERTY, String.class, "");
    }

    private static void fillConnectionOptions(final Dictionary<String, ?> resourceConfig,
                                              final Map<String, String> parameters){
        final Enumeration<String> propertyNames = resourceConfig.keys();
        while (propertyNames.hasMoreElements()) {
            final String name = propertyNames.nextElement();
            switch (name) {
                default:
                    final Object value = resourceConfig.get(name);
                    if (value != null)
                        parameters.put(name, value.toString());
                case CONNECTION_STRING_PROPERTY:
                case ATTRIBUTES_PROPERTY:
                case EVENTS_PROPERTY:
                case RESOURCE_NAME_PROPERTY:
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
            }
        }
    }

    public static Map<String, String> getResourceConnectorParameters(final Dictionary<String, ?> resourceConfig){
        final Map<String, String> result = Maps.newHashMapWithExpectedSize(resourceConfig.size());
        fillConnectionOptions(resourceConfig, result);
        return result;
    }

    private static ConfigurationEntry<ManagedResourceConfiguration> readResourceConfiguration(
            final String factoryPID,
            final Dictionary<String, ?> config) throws IOException {
        final SerializableManagedResourceConfiguration result = new SerializableManagedResourceConfiguration();
        result.setConnectionString(getConnectionString(config));
        result.setConnectionType(getConnectorType(factoryPID));
        //deserialize attributes
        byte[] content = Utils.getProperty(config, ATTRIBUTES_PROPERTY, byte[].class, (byte[]) null);
        if (content != null && content.length > 0)
            try (final ByteArrayInputStream stream = new ByteArrayInputStream(content);
                 final ObjectInputStream deserializer = new ObjectInputStream(stream)) {
                final Map<String, AttributeConfiguration> attributes = TypeTokens.safeCast(deserializer.readObject(), ATTRS_MAP_TYPE);
                if (attributes != null)
                    result.setAttributes(attributes);
            } catch (final ClassNotFoundException e) {
                throw new IOException(e);
            }
        //deserialize events
        content = Utils.getProperty(config, EVENTS_PROPERTY, byte[].class, (byte[]) null);
        if (content != null && content.length > 0)
            try (final ByteArrayInputStream stream = new ByteArrayInputStream(content);
                 final ObjectInputStream deserializer = new ObjectInputStream(stream)) {
                final Map<String, EventConfiguration> events = TypeTokens.safeCast(deserializer.readObject(), EVENTS_MAP_TYPE);
                if (events != null)
                    result.setEvents(events);
            } catch (final ClassNotFoundException e) {
                throw new IOException(e);
            }
        //deserialize parameters
        fillConnectionOptions(config, result.getParameters());
        result.reset();
        return new ConfigurationEntry<ManagedResourceConfiguration>(getResourceName(config),
                result);
    }

    private static ConfigurationEntry<ManagedResourceConfiguration> readResourceConfiguration(final Configuration config) throws PersistentConfigurationException {
        try {
            return readResourceConfiguration(config.getFactoryPid(), config.getProperties());
        } catch (final IOException e) {
            throw new PersistentConfigurationException(config.getPid(), SerializableManagedResourceConfiguration.class, e);
        }
    }

    private static <E extends Exception> void forEachResource(final ConfigurationAdmin admin,
                                                              final String resourceFilter,
                                                              final Consumer<Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        final Configuration[] configs = admin.listConfigurations(resourceFilter);
        if (configs != null && configs.length > 0)
            for (final Configuration config : configs)
                reader.accept(config);
    }

    public static ManagedResourceConfiguration readResourceConfiguration(final ConfigurationAdmin admin,
                                                                         final String resourceName) throws IOException {
        final Box<ManagedResourceConfiguration> result = new Box<>();
        try {
            forEachResource(admin, String.format("(%s=%s)", RESOURCE_NAME_PROPERTY, resourceName), new Consumer<Configuration, PersistentConfigurationException>() {
                @Override
                public void accept(final Configuration config) throws PersistentConfigurationException {
                    final ConfigurationEntry<ManagedResourceConfiguration> entry = readResourceConfiguration(config);
                    result.set(entry.getValue());
                }
            });
        } catch (final InvalidSyntaxException ignored) {
            result.set(null);
        }
        return result.get();
    }

    private static <E extends Exception> void forEachResource(final ConfigurationAdmin admin,
                                                              final Consumer<Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        forEachResource(admin, ALL_CONNECTORS_QUERY, reader);
    }

    public static void forEachResource(final ConfigurationAdmin admin,
                                     final RecordReader<String, ManagedResourceConfiguration, ? extends Exception> reader) throws Exception{
        forEachResource(admin, new Consumer<Configuration, Exception>() {
            @Override
            public void accept(final Configuration config) throws Exception {
                final ConfigurationEntry<ManagedResourceConfiguration> entry = readResourceConfiguration(config);
                reader.read(entry.getKey(), entry.getValue());
            }
        });
    }

    private static void readResources(final ConfigurationAdmin admin,
                                      final Map<String, ManagedResourceConfiguration> output) throws Exception {
        forEachResource(admin, new RecordReader<String, ManagedResourceConfiguration, ExceptionPlaceholder>() {
            @Override
            public void read(final String resourceName, final ManagedResourceConfiguration config) {
                output.put(resourceName, config);
            }
        });
    }

    /**
     * Reads the whole SNAMP configuration.
     * @param admin The configuration admin used to read the whole SNAMP configuration. Cannot be {@literal null}.
     * @return An instance of SNAMP configuration.
     * @throws IOException Unable to read SNAMP configuration.
     */
    public static SerializableAgentConfiguration load(final ConfigurationAdmin admin) throws IOException {
        final SerializableAgentConfiguration result = new SerializableAgentConfiguration();
        try {
            readAdapters(admin, result.getResourceAdapters());
            readResources(admin, result.getManagedResources());
        }
        catch (final IOException e){
            throw e;
        }
        catch (final Exception e) {
            throw new IOException(e);
        }
        result.reset();
        return result;
    }

    private static void save(final String adapterInstanceName,
                             final ResourceAdapterConfiguration adapter,
                             final Configuration output) throws IOException{
        final Dictionary<String, String> result = new Hashtable<>(2);
        Utils.setProperty(result, ADAPTER_INSTANCE_NAME_PROPERTY, adapterInstanceName);
        for(final Map.Entry<String, String> entry: adapter.getParameters().entrySet())
            switch (entry.getKey()) {
                default: result.put(entry.getKey(), entry.getValue());
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
            }
        output.update(result);
    }

    private static void save(final String adapterInstance,
                             final ResourceAdapterConfiguration adapter,
                             final ConfigurationAdmin admin) throws PersistentConfigurationException {
        try {
            //find existing configuration of adapters
            final Box<Boolean> updated = new Box<>(Boolean.FALSE);
            forEachAdapter(admin, String.format("(%s=%s)", ADAPTER_INSTANCE_NAME_PROPERTY, adapterInstance), new Consumer<Configuration, IOException>() {
                @Override
                public void accept(final Configuration config) throws IOException {
                    save(adapterInstance, adapter, config);
                    updated.set(Boolean.TRUE);
                }
            });
            //no existing configuration, creates a new configuration
            if(!updated.get())
                save(adapterInstance,
                        adapter,
                        admin.createFactoryConfiguration(getAdapterFactoryPersistentID(adapter.getAdapterName()), null));
        }
        catch (final IOException | InvalidSyntaxException e) {
            throw new PersistentConfigurationException(adapterInstance, ResourceAdapterConfiguration.class, e);
        }
    }

    private static void save(final String resourceName,
                             final ManagedResourceConfiguration resource,
                             final Configuration output) throws IOException{
        final Dictionary<String, Object> result = new Hashtable<>(4);
        Utils.setProperty(result, CONNECTION_STRING_PROPERTY, resource.getConnectionString());
        Utils.setProperty(result, RESOURCE_NAME_PROPERTY, resourceName);
        final Map<String, AttributeConfiguration> attributes = resource.getElements(AttributeConfiguration.class);
        //serialize attributes
        if (attributes != null)
            try (final ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
                 final ObjectOutputStream serializer = new ObjectOutputStream(os)) {
                serializer.writeObject(attributes);
                serializer.flush();
                Utils.setProperty(result, ATTRIBUTES_PROPERTY, os.toByteArray());
            }
        final Map<String, EventConfiguration> events = resource.getElements(EventConfiguration.class);
        //serialize events
        if (events != null)
            try (final ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
                 final ObjectOutputStream serializer = new ObjectOutputStream(os)) {
                serializer.writeObject(events);
                serializer.flush();
                Utils.setProperty(result, EVENTS_PROPERTY, os.toByteArray());
            }
        //serialize properties
        for(final Map.Entry<String, String> entry: resource.getParameters().entrySet())
            switch (entry.getKey()) {
                default: result.put(entry.getKey(), entry.getValue());
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
            }
        output.update(result);
    }

    private static void save(final String resourceName,
                             final ManagedResourceConfiguration resource,
                             final ConfigurationAdmin admin) throws PersistentConfigurationException{
        try {
            final Box<Boolean> updated = new Box<>(Boolean.FALSE);
            //find existing configuration of resources
            forEachResource(admin, String.format("(%s=%s)", RESOURCE_NAME_PROPERTY, resourceName), new Consumer<Configuration, IOException>() {
                @Override
                public void accept(final Configuration config) throws IOException {
                    save(resourceName, resource, config);
                    updated.set(Boolean.TRUE);
                }
            });
            //no existing configuration, creates a new configuration
            if(!updated.get())
                save(resourceName, resource, admin.createFactoryConfiguration(getConnectorFactoryPersistentID(resource.getConnectionType()), null));
        } catch (final IOException | InvalidSyntaxException e) {
            throw new PersistentConfigurationException(resourceName, ManagedResourceConfiguration.class, e);
        }
    }

    public static void save(final SerializableAgentConfiguration config, final ConfigurationAdmin output) throws IOException{
        if(config.isEmpty())try{
            forEachResource(output, clearAllConsumer);
            forEachAdapter(output, clearAllConsumer);
        }
        catch (final InvalidSyntaxException e){
            throw new IOException(e);
        }
        else {
            try {
                //remove all unnecessary resources
                forEachResource(output, new Consumer<Configuration, IOException>() {
                    private final Map<String, ManagedResourceConfiguration> resources = config.getManagedResources();

                    @Override
                    public void accept(final Configuration config) throws IOException {
                        final String resourceName = Utils.getProperty(config.getProperties(), RESOURCE_NAME_PROPERTY, String.class, "");
                        if (!resources.containsKey(resourceName))
                            config.delete();
                    }
                });
                //remove all unnecessary adapters
                forEachAdapter(output, new Consumer<Configuration, IOException>() {
                    private final Map<String, ResourceAdapterConfiguration> adapters = config.getResourceAdapters();

                    @Override
                    public void accept(final Configuration config) throws IOException {
                        final String adapterInstance = Utils.getProperty(config.getProperties(), ADAPTER_INSTANCE_NAME_PROPERTY, String.class, "");
                        if (!adapters.containsKey(adapterInstance))
                            config.delete();
                    }
                });
            } catch (final InvalidSyntaxException e) {
                throw new IOException(e);
            }
            //save each modified resource or adapter
            config.modifiedAdapters(new RecordReader<String, ResourceAdapterConfiguration, IOException>() {
                @Override
                public void read(final String adapterInstance, final ResourceAdapterConfiguration config) throws IOException {
                    if (config instanceof Modifiable && ((Modifiable) config).isModified())
                        save(adapterInstance, config, output);
                }
            });
            config.modifiedResources(new RecordReader<String, ManagedResourceConfiguration, IOException>() {
                @Override
                public void read(final String resourceName, final ManagedResourceConfiguration config) throws IOException {
                    if (config instanceof Modifiable && ((Modifiable) config).isModified())
                        save(resourceName, config, output);
                }
            });
        }
    }

    /**
     * Returns the currently loaded configuration.
     *
     * @return The currently loaded configuration.
     */
    @Override
    @Aggregation
    public SerializableAgentConfiguration getCurrentConfiguration() {
        return configuration;
    }

    /**
     * Reads SNAMP configuration.
     * @throws IOException Unable to restore SNAMP configuration.
     */
    public synchronized void load() throws IOException{
        configuration = load(admin);
    }

    /**
     * Reload agent configuration from the persistent storage.
     * @deprecated Use {@link #load()} instead of this method because this
     * method suppresses {@link java.io.IOException}.
     */
    @Override
    @Deprecated
    public void reload() {
        try {
            load();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Failed to read SNAMP configuration", e);
        }
    }

    /**
     * Saves SNAMP configuration into OSGi persistent storage.
     * @throws IOException Some I/O error occurs.
     */
    public synchronized void save() throws IOException{
        if(configuration != null)
            save(configuration, admin);
    }

    /**
     * Dumps the agent configuration into the persistent storage.
     * @deprecated Use {@link #save()} method because this method suppresses {@link java.io.IOException}.
     */
    @Override
    @Deprecated
    public synchronized void sync() {
        try {
            save();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Unable to save SNAMP configuration", e);
        }
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    @Aggregation
    public Logger getLogger() {
        return logger;
    }
}
