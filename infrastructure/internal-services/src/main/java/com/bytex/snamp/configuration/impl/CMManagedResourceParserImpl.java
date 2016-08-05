package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.*;
import com.bytex.snamp.configuration.internal.CMManagedResourceParser;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;

import static com.bytex.snamp.configuration.impl.SerializableManagedResourceConfiguration.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMManagedResourceParserImpl implements CMManagedResourceParser {
    private static final TypeToken<SerializableMap<String, SerializableAttributeConfiguration>> ATTRS_MAP_TYPE = new TypeToken<SerializableMap<String, SerializableAttributeConfiguration>>() {};
    private static final TypeToken<SerializableMap<String, SerializableEventConfiguration>> EVENTS_MAP_TYPE = new TypeToken<SerializableMap<String, SerializableEventConfiguration>>() {};
    private static final TypeToken<SerializableMap<String, SerializableOperationConfiguration>> OPS_MAP_TYPE = new TypeToken<SerializableMap<String, SerializableOperationConfiguration>>() {};

    private static final String CONNECTOR_PID_TEMPLATE = "com.bytex.snamp.connectors.%s";
    private static final String RESOURCE_NAME_PROPERTY = "$resourceName$";
    private static final String CONNECTION_STRING_PROPERTY = "$connectionString$";
    private static final String ATTRIBUTES_PROPERTY = "$attributes$";
    private static final String EVENTS_PROPERTY = "$events$";
    private static final String OPERATIONS_PROPERTY = "$operations$";

    private static final String ALL_CONNECTORS_QUERY = String.format("(%s=%s)", Constants.SERVICE_PID, String.format(CONNECTOR_PID_TEMPLATE, "*"));

    private static final class ManagedResourceConfigurationException extends PersistentConfigurationException{
        private static final long serialVersionUID = -8618780912903622327L;

        private ManagedResourceConfigurationException(final String pid, final Throwable e) {
            super(pid, SerializableManagedResourceConfiguration.class, e);
        }
    }

    /**
     * Returns managed connector persistent identifier.
     * @param connectorType The type of the managed resource connector.
     * @return The persistent identifier.
     */
    @Override
    public String getConnectorFactoryPersistentID(final String connectorType){
        return String.format(CONNECTOR_PID_TEMPLATE, connectorType);
    }

    /**
     * Returns managed resource name by its persistent identifier.
     * @param factoryPID Managed resource persistent identifier.
     * @return The name of the managed resource.
     */
    private static String getConnectorType(final String factoryPID){
        return factoryPID.replaceFirst(String.format(CONNECTOR_PID_TEMPLATE, ""), "");
    }

    @Override
    public String getConnectionString(final Dictionary<String, ?> resourceConfig) {
        return Utils.getProperty(resourceConfig, CONNECTION_STRING_PROPERTY, String.class, "");
    }

    @Override
    public String getResourceName(final Dictionary<String, ?> resourceConfig) {
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

    @Override
    public Map<String, String> getResourceConnectorParameters(final Dictionary<String, ?> resourceConfig) {
        final Map<String, String> result = Maps.newHashMapWithExpectedSize(resourceConfig.size());
        fillConnectionOptions(resourceConfig, result);
        return result;
    }

    private <F extends FeatureConfiguration> Map<String, F> getFeatures(final Dictionary<String, ?> resourceConfig,
                                                                               final String featureHolder,
                                                                               final TypeToken<SerializableMap<String, F>> featureType) throws IOException{
        byte[] serializedForm = Utils.getProperty(resourceConfig,
                featureHolder,
                byte[].class,
                ArrayUtils.emptyArray(byte[].class));
        return serializedForm != null && serializedForm.length > 0 ?
                IOUtils.deserialize(serializedForm, featureType, getClass().getClassLoader()):
                ImmutableMap.of();
    }

    @Override
    public Map<String, SerializableAttributeConfiguration> getAttributes(final Dictionary<String, ?> resourceConfig) throws IOException {
        return getFeatures(resourceConfig, ATTRIBUTES_PROPERTY, ATTRS_MAP_TYPE);
    }

    @Override
    public Map<String, SerializableOperationConfiguration> getOperations(final Dictionary<String, ?> resourceConfig) throws IOException {
        return getFeatures(resourceConfig, OPERATIONS_PROPERTY, OPS_MAP_TYPE);
    }

    @Override
    public Map<String, SerializableEventConfiguration> getEvents(final Dictionary<String, ?> resourceConfig) throws IOException {
        return getFeatures(resourceConfig, EVENTS_PROPERTY, EVENTS_MAP_TYPE);
    }

    private static <E extends Exception> void forEachResource(final ConfigurationAdmin admin,
                                                              final String filter,
                                                              final Acceptor<Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        final Configuration[] configs = admin.listConfigurations(filter);
        if (configs != null && configs.length > 0)
            for (final Configuration config : configs)
                reader.accept(config);
    }

    void removeAll(final ConfigurationAdmin admin) throws IOException {
        try {
            forEachResource(admin, ALL_CONNECTORS_QUERY, Configuration::delete);
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    void readResources(final ConfigurationAdmin admin,
                                      final Map<String, SerializableManagedResourceConfiguration> output) throws IOException {
        try {
            forEachResource(admin, ALL_CONNECTORS_QUERY, config -> {
                final String resourceName = getResourceName(config.getProperties());
                final SerializableManagedResourceConfiguration resource = parse(config);
                output.put(resourceName, resource);
            });
        } catch (final InvalidSyntaxException e) {
           throw new IOException(e);
        }
    }

    @Override
    public SerializableManagedResourceConfiguration parse(final Configuration config) throws IOException {
        final SerializableManagedResourceConfiguration result = new SerializableManagedResourceConfiguration();
        result.setConnectionString(getConnectionString(config.getProperties()));
        result.setConnectionType(getConnectorType(config.getFactoryPid()));
        //deserialize attributes
        result.setAttributes(getAttributes(config.getProperties()));
        //deserialize events
        result.setEvents(getEvents(config.getProperties()));
        //deserialize operations
        result.setOperations(getOperations(config.getProperties()));
        //deserialize parameters
        fillConnectionOptions(config.getProperties(), result.getParameters());
        result.reset();
        return result;
    }

    private static void serialize(final String resourceName,
                   final SerializableManagedResourceConfiguration resource,
                   final Configuration output) throws IOException {
        final Dictionary<String, Object> configuration = serialize(resource);
        Utils.setProperty(configuration, RESOURCE_NAME_PROPERTY, resourceName);
        output.update(configuration);
    }

    private static Dictionary<String, Object> serialize(final SerializableManagedResourceConfiguration resource) throws IOException {
        final Dictionary<String, Object> result = new Hashtable<>(4);
        Utils.setProperty(result, CONNECTION_STRING_PROPERTY, resource.getConnectionString());
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
        return result;
    }

    @Override
    public void serialize(final ManagedResourceConfiguration input, final Configuration output) throws IOException {
        assert input instanceof SerializableManagedResourceConfiguration;
        output.update(serialize((SerializableManagedResourceConfiguration) input));
    }

    private void serialize(final String resourceName,
                             final SerializableManagedResourceConfiguration resource,
                             final ConfigurationAdmin admin) throws ManagedResourceConfigurationException {
        try {
            final Box<Boolean> updated = new Box<>(Boolean.FALSE);
            //find existing configuration of resources
            forEachResource(admin, String.format("(%s=%s)", RESOURCE_NAME_PROPERTY, resourceName), config -> {
                    serialize(resourceName, resource, config);
                    updated.set(Boolean.TRUE);
            });
            //no existing configuration, creates a new configuration
            if(!updated.get())
                serialize(resourceName, resource, admin.createFactoryConfiguration(getConnectorFactoryPersistentID(resource.getConnectionType()), null));
        } catch (final IOException | InvalidSyntaxException e) {
            throw new ManagedResourceConfigurationException(resourceName, e);
        }
    }

    void saveChanges(final SerializableAgentConfiguration config,
                     final ConfigurationAdmin admin) throws IOException {
        //remove all unnecessary resources
        final Map<String, ? extends SerializableManagedResourceConfiguration> resources = config.getEntities(SerializableManagedResourceConfiguration.class);
        try {
            forEachResource(admin, ALL_CONNECTORS_QUERY, output -> {
                final String resourceName = getResourceName(output.getProperties());
                if (!resources.containsKey(resourceName))
                    output.delete();
            });
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
        //save each modified resource
        config.modifiedResources((resourceName, resource) -> {
            if (resource.isModified())
                serialize(resourceName, resource, admin);
            return true;
        });
    }
}
