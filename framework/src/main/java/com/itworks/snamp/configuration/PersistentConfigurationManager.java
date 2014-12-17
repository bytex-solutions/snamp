package com.itworks.snamp.configuration;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.mapping.RecordReader;
import com.itworks.snamp.mapping.TypeLiterals;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
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
    private static final String PARAMS_PROPERTY = "parameters";
    private static final TypeToken<Map<String, String>> PARAMS_MAP_TYPE = new TypeToken<Map<String, String>>() {};
    private static final TypeToken<Map<String, AttributeConfiguration>> ATTRS_MAP_TYPE = new TypeToken<Map<String, AttributeConfiguration>>() {};
    private static final TypeToken<Map<String, EventConfiguration>> EVENTS_MAP_TYPE = new TypeToken<Map<String, EventConfiguration>>() {};

    private static final String ADAPTER_PID_TEMPLATE = "com.itworks.snamp.adapters.%s";
    private static final String ADAPTER_NAME_PROPERTY = "adapterName";
    private static final String ALL_ADAPTERS_QUERY = String.format("(%s=%s)", Constants.SERVICE_PID, String.format(ADAPTER_PID_TEMPLATE, "*"));

    private static final String CONNECTOR_PID_TEMPLATE = "com.itworks.snamp.connectors.%s";
    private static final String CONNECTOR_TYPE_PROPERTY = "connectorType";
    private static final String ALL_CONNECTORS_QUERY = String.format("(%s=%s)", Constants.SERVICE_PID, String.format(CONNECTOR_PID_TEMPLATE, "*"));
    private static final String CONNECTION_STRING_PROPERTY = "connectionString";
    private static final String ATTRIBUTES_PROPERTY = "attributes";
    private static final String EVENTS_PROPERTY = "events";

    /**
     * Represents an exception happens when persistent configuration manager cannot
     * restore SNAMP configuration entity from OSGi persistent configuration store.
     * This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class PersistentConfigurationException extends IOException{
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
     * @param adapterInstance The name of the adapter instance.
     * @return Persistent identifier.
     */
    public static String getAdapterPersistentID(final String adapterInstance){
        return String.format(ADAPTER_PID_TEMPLATE, adapterInstance);
    }

    /**
     * Returns managed resource persistent identifier.
     * @param resourceName The name of the managed resource.
     * @return The persistent identifier.
     */
    public static String getResourcePersistentID(final String resourceName){
        return String.format(CONNECTOR_PID_TEMPLATE, resourceName);
    }

    /**
     * Returns name of the resource adapter instance by its persistent identifier.
     * @param pid Resource adapter persistent identifier.
     * @return The name of the resource adapter.
     */
    public static String getAdapterInstanceName(final String pid){
        return pid.replaceFirst(String.format(ADAPTER_PID_TEMPLATE, ""), "");
    }

    /**
     * Returns managed resource name by its persistent identifier.
     * @param pid Managed resource persistent identifier.
     * @return The name of the managed resource.
     */
    public static String getResourceName(final String pid){
        return pid.replaceFirst(String.format(CONNECTOR_PID_TEMPLATE, ""), "");
    }

    /**
     * Reads configuration of the resource adapter.
     * @param config OSGi persistent configuration used as a SNAMP configuration source. Cannot be {@literal null}.
     * @return An instance of the resource adapter configuration.
     * @throws IOException Unable to restore adapter configuration.
     */
    public static ResourceAdapterConfiguration readAdapterConfiguration(final Dictionary<String, ?> config) throws IOException {
        final SerializableResourceAdapterConfiguration result = new SerializableResourceAdapterConfiguration();
        result.setAdapterName(Utils.getProperty(config, ADAPTER_NAME_PROPERTY, String.class, ""));
        final byte[] serializedParams = Utils.getProperty(config, PARAMS_PROPERTY, byte[].class, (byte[])null);
        if(serializedParams != null && serializedParams.length > 0)
                try(final ByteArrayInputStream stream = new ByteArrayInputStream(serializedParams);
                    final ObjectInputStream deserializer = new ObjectInputStream(stream)){
                    final Map<String, String> parameters = TypeLiterals.safeCast(deserializer.readObject(), PARAMS_MAP_TYPE);
                    if(parameters != null) result.setParameters(parameters);
                }
                catch (final ClassNotFoundException e){
                    throw new IOException(e);
                }
        result.reset();
        return result;
    }

    private static ResourceAdapterConfiguration readAdapterConfiguration(final Configuration config) throws PersistentConfigurationException {
        try {
            return readAdapterConfiguration(config.getProperties());
        } catch (final IOException e) {
            throw new PersistentConfigurationException(config.getPid(), SerializableResourceAdapterConfiguration.class, e);
        }
    }

    private static <E extends Exception> void forEachAdapter(final ConfigurationAdmin admin,
                                                             final String filter,
                                                             final RecordReader<String, Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        final Configuration[] configs = admin.listConfigurations(filter);
        if(configs != null && configs.length > 0)
            for(final Configuration config: configs) {
                final String adapterInstance = getAdapterInstanceName(config.getPid());
                if (adapterInstance == null || adapterInstance.isEmpty())
                    config.delete();
                else reader.read(adapterInstance, config);
            }
    }

    private static <E extends Exception> void forEachAdapter(final ConfigurationAdmin admin,
                                                             final RecordReader<String, Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        forEachAdapter(admin, ALL_ADAPTERS_QUERY, reader);
    }

    private static void readAdapters(final ConfigurationAdmin admin,
                                     final Map<String, ResourceAdapterConfiguration> output) throws IOException, InvalidSyntaxException {
        forEachAdapter(admin, new RecordReader<String, Configuration, IOException>() {
            @Override
            public void read(final String adapterInstance, final Configuration config) throws IOException {
                output.put(adapterInstance, readAdapterConfiguration(config));
            }
        });
    }

    public static ManagedResourceConfiguration readResourceConfiguration(final Dictionary<String, ?> config) throws IOException {
        final SerializableManagedResourceConfiguration result = new SerializableManagedResourceConfiguration();
        result.setConnectionString(Utils.getProperty(config, CONNECTION_STRING_PROPERTY, String.class, ""));
        result.setConnectionType(Utils.getProperty(config, CONNECTOR_TYPE_PROPERTY, String.class, ""));
        //deserialize attributes
        byte[] content = Utils.getProperty(config, ATTRIBUTES_PROPERTY, byte[].class, (byte[])null);
        if(content != null && content.length > 0)
            try(final ByteArrayInputStream stream = new ByteArrayInputStream(content);
                final ObjectInputStream deserializer = new ObjectInputStream(stream)){
                final Map<String, AttributeConfiguration> attributes = TypeLiterals.safeCast(deserializer.readObject(), ATTRS_MAP_TYPE);
                if(attributes != null)
                    result.setAttributes(attributes);
            }
            catch (final ClassNotFoundException e){
                throw new IOException(e);
            }
        //deserialize events
        content = Utils.getProperty(config, EVENTS_PROPERTY, byte[].class, (byte[])null);
        if(content != null && content.length > 0)
            try(final ByteArrayInputStream stream = new ByteArrayInputStream(content);
                final ObjectInputStream deserializer = new ObjectInputStream(stream)){
                final Map<String, EventConfiguration> events = TypeLiterals.safeCast(deserializer.readObject(), EVENTS_MAP_TYPE);
                if(events != null)
                    result.setEvents(events);
            }
            catch (final ClassNotFoundException e){
                throw new IOException(e);
            }
        result.reset();
        return result;
    }

    private static ManagedResourceConfiguration readResourceConfiguration(final Configuration config) throws PersistentConfigurationException {
        try {
            return readResourceConfiguration(config.getProperties());
        } catch (final IOException e) {
            throw new PersistentConfigurationException(config.getPid(), SerializableManagedResourceConfiguration.class, e);
        }
    }

    private static <E extends Exception> void forEachResource(final ConfigurationAdmin admin,
                                                              final String resourceFilter,
                                                              final RecordReader<String, Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        final Configuration[] configs = admin.listConfigurations(resourceFilter);
        if (configs != null && configs.length > 0)
            for (final Configuration config : configs) {
                final String resourceName = getResourceName(config.getPid());
                if (resourceName == null || resourceName.isEmpty())
                    config.delete();
                else reader.read(resourceName, config);
            }
    }

    private static <E extends Exception> void forEachResource(final ConfigurationAdmin admin,
                                                             final RecordReader<String, Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        forEachResource(admin, ALL_CONNECTORS_QUERY, reader);
    }

    private static void readResources(final ConfigurationAdmin admin,
                                      final Map<String, ManagedResourceConfiguration> output) throws IOException, InvalidSyntaxException {
        forEachResource(admin, new RecordReader<String, Configuration, IOException>() {
            @Override
            public void read(final String resourceName, final Configuration config) throws IOException {
                output.put(resourceName, readResourceConfiguration(config));
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
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
        result.reset();
        return result;
    }

    private static Dictionary<String, Object> toDictionary(final ResourceAdapterConfiguration adapter) throws IOException{
        final Dictionary<String, Object> result = new Hashtable<>(2);
        Utils.setProperty(result, ADAPTER_NAME_PROPERTY, adapter.getAdapterName());
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            final ObjectOutputStream serializer = new ObjectOutputStream(os)){
            serializer.writeObject(adapter.getParameters());
            serializer.flush();
            Utils.setProperty(result, PARAMS_PROPERTY, os.toByteArray());
        }
        return result;
    }

    private static void save(final ResourceAdapterConfiguration adapter, final Configuration output) throws IOException{
        output.update(toDictionary(adapter));
    }

    private static void save(final String adapterInstance, final ResourceAdapterConfiguration config, final ConfigurationAdmin admin) throws PersistentConfigurationException {
        final String pid = getAdapterPersistentID(adapterInstance);
        try {
            save(config, admin.getConfiguration(pid));
        } catch (final IOException e) {
            throw new PersistentConfigurationException(pid, ResourceAdapterConfiguration.class, e);
        }
    }

    private static Dictionary<String, Object> toDictionary(final ManagedResourceConfiguration resource) throws IOException {
        final Dictionary<String, Object> result = new Hashtable<>(4);
        Utils.setProperty(result, CONNECTOR_TYPE_PROPERTY, resource.getConnectionType());
        Utils.setProperty(result, CONNECTION_STRING_PROPERTY, resource.getConnectionString());
        final Map<String, AttributeConfiguration> attributes = resource.getElements(AttributeConfiguration.class);
        if (attributes != null)
            try (final ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
                 final ObjectOutputStream serializer = new ObjectOutputStream(os)) {
                serializer.writeObject(attributes);
                serializer.flush();
                Utils.setProperty(result, ATTRIBUTES_PROPERTY, os.toByteArray());
            }
        final Map<String, EventConfiguration> events = resource.getElements(EventConfiguration.class);
        if (events != null)
            try (final ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
                 final ObjectOutputStream serializer = new ObjectOutputStream(os)) {
                serializer.writeObject(attributes);
                serializer.flush();
                Utils.setProperty(result, EVENTS_PROPERTY, os.toByteArray());
            }
        return result;
    }

    private static void save(final ManagedResourceConfiguration resource, final Configuration output) throws IOException{
        output.update(toDictionary(resource));
    }

    private static void save(final String resourceName, final ManagedResourceConfiguration config, final ConfigurationAdmin admin) throws PersistentConfigurationException{
        final String pid = getResourcePersistentID(resourceName);
        try {
            save(config, admin.getConfiguration(pid));
        } catch (final IOException e) {
            throw new PersistentConfigurationException(pid, ManagedResourceConfiguration.class, e);
        }
    }

    public static void save(final SerializableAgentConfiguration config, final ConfigurationAdmin output) throws IOException{
        try {
            //remove all unnecessary resources
            forEachResource(output, new RecordReader<String, Configuration, IOException>() {
                private final Map<String, ManagedResourceConfiguration> resources = config.getManagedResources();

                @Override
                public void read(final String resourceName, final Configuration config) throws IOException {
                    if (!resources.containsKey(resourceName))
                        config.delete();
                }
            });
            //remove all unnecessary adapters
            forEachAdapter(output, new RecordReader<String, Configuration, IOException>() {
                private final Map<String, ResourceAdapterConfiguration> adapters = config.getResourceAdapters();

                @Override
                public void read(final String adapterInstance, final Configuration config) throws IOException {
                    if(!adapters.containsKey(adapterInstance))
                        config.delete();
                }
            });
        }
        catch (final InvalidSyntaxException e){
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

    /**
     * Extracts managed resources filtered by connector type.
     * @param admin The configuration admin used to read the whole SNAMP configuration. Cannot be {@literal null}.
     * @param connectorType Resource connector type.
     * @param reader Managed resource reader.
     * @throws Exception Unable to read SNAMP configuration.
     */
    public static void findResourcesByType(final ConfigurationAdmin admin,
                                           final String connectorType,
                                           final RecordReader<String, ManagedResourceConfiguration, ? extends Exception> reader) throws Exception {
        forEachResource(admin, String.format("(&(%s=%s)(%s=%s))", CONNECTOR_TYPE_PROPERTY, connectorType,
                        Constants.SERVICE_PID, String.format(CONNECTOR_PID_TEMPLATE, "*")),
                new RecordReader<String, Configuration, Exception>() {
                    @Override
                    public void read(final String resourceName, final Configuration config) throws Exception {
                        reader.read(resourceName, readResourceConfiguration(config));
                    }
                });
    }

    /**
     * Extracts resource adapters filtered by adapter name.
     * @param admin The configuration admin used to read the whole SNAMP configuration. Cannot be {@literal null}.
     * @param adapterName The name of the resource adapter.
     * @param reader Resource adapter reader.
     * @throws Exception Unable to read SNAMP configuration.
     */
    public static void findAdaptersByName(final ConfigurationAdmin admin,
                                          final String adapterName,
                                          final RecordReader<String, ResourceAdapterConfiguration, ? extends Exception> reader) throws Exception {
        forEachAdapter(admin, String.format("(&(%s=%s)(%s=%s))", ADAPTER_NAME_PROPERTY, adapterName,
                        Constants.SERVICE_PID, String.format(ADAPTER_PID_TEMPLATE, "*")),
                new RecordReader<String, Configuration, Exception>() {
                    @Override
                    public void read(final String adapterInstance, final Configuration config) throws Exception {
                        reader.read(adapterInstance, readAdapterConfiguration(config));
                    }
                });
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
