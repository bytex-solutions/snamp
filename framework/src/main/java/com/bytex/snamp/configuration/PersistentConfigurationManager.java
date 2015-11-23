package com.bytex.snamp.configuration;

import com.bytex.snamp.core.ServiceHolder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.bytex.snamp.*;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.internal.EntryReader;
import com.bytex.snamp.io.IOUtils;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.AgentConfiguration.*;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;
import static com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration;
import static com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableResourceAdapterConfiguration;

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
    private static final TypeToken<SerializableMap<String, SerializableManagedResourceConfiguration.SerializableAttributeConfiguration>> ATTRS_MAP_TYPE = new TypeToken<SerializableMap<String, SerializableManagedResourceConfiguration.SerializableAttributeConfiguration>>() {};
    private static final TypeToken<SerializableMap<String, SerializableManagedResourceConfiguration.SerializableEventConfiguration>> EVENTS_MAP_TYPE = new TypeToken<SerializableMap<String, SerializableManagedResourceConfiguration.SerializableEventConfiguration>>() {};
    private static final TypeToken<SerializableMap<String, SerializableManagedResourceConfiguration.SerializableOperationConfiguration>> OPS_MAP_TYPE = new TypeToken<SerializableMap<String, SerializableManagedResourceConfiguration.SerializableOperationConfiguration>>() {};

    private static final String ADAPTER_PID_TEMPLATE = "com.bytex.snamp.adapters.%s";
    private static final String ADAPTER_INSTANCE_NAME_PROPERTY = "$adapterInstanceName$";
    private static final String ALL_ADAPTERS_QUERY = String.format("(%s=%s)", Constants.SERVICE_PID, String.format(ADAPTER_PID_TEMPLATE, "*"));

    private static final String CONNECTOR_PID_TEMPLATE = "com.bytex.snamp.connectors.%s";
    private static final String RESOURCE_NAME_PROPERTY = "$resourceName$";
    private static final String ALL_CONNECTORS_QUERY = String.format("(%s=%s)", Constants.SERVICE_PID, String.format(CONNECTOR_PID_TEMPLATE, "*"));
    private static final String CONNECTION_STRING_PROPERTY = "$connectionString$";
    private static final String ATTRIBUTES_PROPERTY = "$attributes$";
    private static final String EVENTS_PROPERTY = "$events$";
    private static final String OPERATIONS_PROPERTY = "$operations";

    private static final Consumer<Configuration, IOException> CLEAR_CONFIG_CONSUMER = new Consumer<Configuration, IOException>() {
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
        public final Class<? extends EntityConfiguration> entityType;

        private PersistentConfigurationException(final String pid,
                                                final Class<? extends EntityConfiguration> entityType,
                                                final Throwable e){
            super(String.format("Unable to read SNAMP %s configuration", pid), e);
            this.persistenceID = pid;
            this.entityType = entityType;
        }
    }

    private static final class ConfigurationEntry<V extends EntityConfiguration> implements Map.Entry<String, V>{
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
    private volatile SerializableAgentConfiguration configuration;
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
    public PersistentConfigurationManager(final ServiceHolder<ConfigurationAdmin> configAdmin) {
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
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
                case ADAPTER_INSTANCE_NAME_PROPERTY:
            }
        }
    }

    public static Map<String, String> getAdapterParameters(final Dictionary<String, ?> adapterConfig){
        final Map<String, String> result = Maps.newHashMapWithExpectedSize(adapterConfig.size());
        fillAdapterParameters(adapterConfig, result);
        return result;
    }

    private static ConfigurationEntry<SerializableResourceAdapterConfiguration> readAdapterConfiguration(
            final String factoryPID,
            final Dictionary<String, ?> config) {
        final SerializableResourceAdapterConfiguration result = new SerializableResourceAdapterConfiguration();
        result.setAdapterName(getAdapterType(factoryPID));
        //deserialize parameters
        fillAdapterParameters(config, result.getParameters());
        result.reset();
        return new ConfigurationEntry<>(getAdapterInstanceName(config), result);
    }

    private static ConfigurationEntry<SerializableResourceAdapterConfiguration> readAdapterConfiguration(final Configuration config) {
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

    /**
     * Iterates through all configured resources adapters sequentially.
     * @param admin Persistent configuration admin. Cannot be {@literal null}.
     * @param reader Adapter configuration reader. Cannot be {@literal null}.
     * @throws Exception Exception raised by reader.
     */
    //used by SNAMP Management Console. Do not remove!!
    public static void forEachAdapter(final ConfigurationAdmin admin,
                                      final EntryReader<String, ResourceAdapterConfiguration, ? extends Exception> reader) throws Exception {
        forEachAdapter(admin, new Consumer<Configuration, Exception>() {
            @Override
            public void accept(final Configuration config) throws Exception {
                final ConfigurationEntry<SerializableResourceAdapterConfiguration> entry = readAdapterConfiguration(config);
                reader.read(entry.getKey(), entry.getValue());
            }
        });
    }

    private static <E extends Exception> void forEachAdapter(final ConfigurationAdmin admin,
                                                             final Consumer<Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        forEachAdapter(admin, ALL_ADAPTERS_QUERY, reader);
    }

    private static void readAdapters(final ConfigurationAdmin admin,
                                     final Map<String, SerializableResourceAdapterConfiguration> output) throws IOException, InvalidSyntaxException {
        forEachAdapter(admin, new Consumer<Configuration, IOException>() {
            @Override
            public void accept(final Configuration config) throws IOException {
                final ConfigurationEntry<SerializableResourceAdapterConfiguration> entry = readAdapterConfiguration(config);
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
                case OPERATIONS_PROPERTY:
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

    private static <F extends FeatureConfiguration> Map<String, F> getFeatures(final Dictionary<String, ?> resourceConfig,
                                                                final String featureHolder,
                                                                final TypeToken<SerializableMap<String, F>> featureType) throws IOException{
        byte[] serializedForm = Utils.getProperty(resourceConfig,
                featureHolder,
                byte[].class,
                ArrayUtils.emptyArray(byte[].class));
        return serializedForm != null && serializedForm.length > 0 ?
                IOUtils.deserialize(serializedForm, featureType):
                ImmutableMap.<String, F>of();
    }

    public static Map<String, SerializableManagedResourceConfiguration.SerializableAttributeConfiguration> getAttributes(final Dictionary<String, ?> resourceConfig) throws IOException{
        return getFeatures(resourceConfig, ATTRIBUTES_PROPERTY, ATTRS_MAP_TYPE);
    }

    public static Map<String, SerializableManagedResourceConfiguration.SerializableOperationConfiguration> getOperations(final Dictionary<String, ?> resourceConfig) throws IOException{
        return getFeatures(resourceConfig, OPERATIONS_PROPERTY, OPS_MAP_TYPE);
    }

    public static Map<String, SerializableManagedResourceConfiguration.SerializableEventConfiguration> getEvents(final Dictionary<String, ?> resourceConfig) throws IOException {
        return getFeatures(resourceConfig, EVENTS_PROPERTY, EVENTS_MAP_TYPE);
    }

    private static ConfigurationEntry<SerializableManagedResourceConfiguration> readResourceConfiguration(
            final String factoryPID,
            final Dictionary<String, ?> config) throws IOException {
        final SerializableManagedResourceConfiguration result = new SerializableManagedResourceConfiguration();
        result.setConnectionString(getConnectionString(config));
        result.setConnectionType(getConnectorType(factoryPID));
        //deserialize attributes
        result.setAttributes(getAttributes(config));
        //deserialize events
        result.setEvents(getEvents(config));
        //deserialize operations
        result.setOperations(getOperations(config));
        //deserialize parameters
        fillConnectionOptions(config, result.getParameters());
        result.reset();
        return new ConfigurationEntry<>(getResourceName(config),
                result);
    }

    private static ConfigurationEntry<SerializableManagedResourceConfiguration> readResourceConfiguration(final Configuration config) throws PersistentConfigurationException {
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
                    final ConfigurationEntry<SerializableManagedResourceConfiguration> entry = readResourceConfiguration(config);
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
                                     final EntryReader<String, SerializableManagedResourceConfiguration, ? extends Exception> reader) throws Exception{
        forEachResource(admin, new Consumer<Configuration, Exception>() {
            @Override
            public void accept(final Configuration config) throws Exception {
                final ConfigurationEntry<SerializableManagedResourceConfiguration> entry = readResourceConfiguration(config);
                reader.read(entry.getKey(), entry.getValue());
            }
        });
    }

    private static void readResources(final ConfigurationAdmin admin,
                                      final Map<String, SerializableManagedResourceConfiguration> output) throws Exception {
        forEachResource(admin, new EntryReader<String, SerializableManagedResourceConfiguration, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final SerializableManagedResourceConfiguration config) {
                output.put(resourceName, config);
                return true;
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
        final Dictionary<String, String> result = new Hashtable<>(4);
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
        final Map<String, ? extends AttributeConfiguration> attributes = resource.getFeatures(AttributeConfiguration.class);
        //serialize attributes
        if (attributes != null)
            Utils.setProperty(result, ATTRIBUTES_PROPERTY, IOUtils.serialize((Serializable)attributes));
        final Map<String, ? extends EventConfiguration> events = resource.getFeatures(EventConfiguration.class);
        //serialize events
        if (events != null)
            Utils.setProperty(result, EVENTS_PROPERTY, IOUtils.serialize((Serializable)events));
        //serialize operations
        final Map<String, ? extends OperationConfiguration> operations = resource.getFeatures(OperationConfiguration.class);
        if(operations != null)
            Utils.setProperty(result, OPERATIONS_PROPERTY, IOUtils.serialize((Serializable)operations));
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
            forEachResource(output, CLEAR_CONFIG_CONSUMER);
            forEachAdapter(output, CLEAR_CONFIG_CONSUMER);
        }
        catch (final InvalidSyntaxException e){
            throw new IOException(e);
        }
        else {
            try {
                //remove all unnecessary adapters
                forEachAdapter(output, new Consumer<Configuration, IOException>() {
                    private final Map<String, SerializableResourceAdapterConfiguration> adapters = config.getResourceAdapters();

                    @Override
                    public void accept(final Configuration config) throws IOException {
                        final String adapterInstance = Utils.getProperty(config.getProperties(), ADAPTER_INSTANCE_NAME_PROPERTY, String.class, "");
                        if (!adapters.containsKey(adapterInstance))
                            config.delete();
                    }
                });
                //remove all unnecessary resources
                forEachResource(output, new Consumer<Configuration, IOException>() {
                    private final Map<String, SerializableManagedResourceConfiguration> resources = config.getManagedResources();

                    @Override
                    public void accept(final Configuration config) throws IOException {
                        final String resourceName = Utils.getProperty(config.getProperties(), RESOURCE_NAME_PROPERTY, String.class, "");
                        if (!resources.containsKey(resourceName))
                            config.delete();
                    }
                });
            } catch (final InvalidSyntaxException e) {
                throw new IOException(e);
            }
            //save each modified resource or adapter
            config.modifiedAdapters(new EntryReader<String, ResourceAdapterConfiguration, IOException>() {
                @Override
                public boolean read(final String adapterInstance, final ResourceAdapterConfiguration config) throws IOException {
                    if (config instanceof Modifiable && ((Modifiable) config).isModified())
                        save(adapterInstance, config, output);
                    return true;
                }
            });
            config.modifiedResources(new EntryReader<String, ManagedResourceConfiguration, IOException>() {
                @Override
                public boolean read(final String resourceName, final ManagedResourceConfiguration config) throws IOException {
                    if (config instanceof Modifiable && ((Modifiable) config).isModified())
                        save(resourceName, config, output);
                    return true;
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
        SerializableAgentConfiguration result = configuration;
        if (result == null)
            synchronized (this) {
                result = configuration;
                if (result == null)
                    result = configuration = new SerializableAgentConfiguration();
            }
        return result;
    }

    public synchronized <E extends Throwable> void processConfiguration(final Consumer<? super SerializableAgentConfiguration, E> handler,
                                                                        final boolean saveChanges) throws E, IOException {
        handler.accept(getCurrentConfiguration());
        if (saveChanges)
            save();
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
    public synchronized void save() throws IOException {
        if (configuration != null)
            save(configuration, admin);
    }

    /**
     * Dumps the agent configuration into the persistent storage.
     * @deprecated Use {@link #save()} method because this method suppresses {@link java.io.IOException}.
     */
    @Override
    @Deprecated
    public void sync() {
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
