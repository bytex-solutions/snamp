package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.MutableBoolean;
import com.bytex.snamp.io.SerializableMap;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.internal.CMManagedResourceParser;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.putValue;
import static com.bytex.snamp.configuration.impl.SerializableManagedResourceConfiguration.*;
import static com.bytex.snamp.connector.ManagedResourceConnector.CAPABILITY_NAMESPACE;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMManagedResourceParserImpl extends AbstractConfigurationParser<SerializableManagedResourceConfiguration> implements CMManagedResourceParser {
    private static final TypeToken<SerializableMap<String, SerializableAttributeConfiguration>> ATTRS_MAP_TYPE = new TypeToken<SerializableMap<String, SerializableAttributeConfiguration>>() {};
    private static final TypeToken<SerializableMap<String, SerializableEventConfiguration>> EVENTS_MAP_TYPE = new TypeToken<SerializableMap<String, SerializableEventConfiguration>>() {};
    private static final TypeToken<SerializableMap<String, SerializableOperationConfiguration>> OPS_MAP_TYPE = new TypeToken<SerializableMap<String, SerializableOperationConfiguration>>() {};

    private static final String CONNECTOR_PID_TEMPLATE = CAPABILITY_NAMESPACE + ".%s";
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
    public String getFactoryPersistentID(final String connectorType){
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


    private String getConnectionString(final Dictionary<String, ?> resourceConfig) {
        return getValue(resourceConfig, CONNECTION_STRING_PROPERTY, Objects::toString, () -> "");
    }

    @Override
    public String getResourceName(final Dictionary<String, ?> resourceConfig) {
        return getValue(resourceConfig, RESOURCE_NAME_PROPERTY, String.class, () -> "");
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

    private <F extends FeatureConfiguration> Map<String, F> getFeatures(final Dictionary<String, ?> resourceConfig,
                                                                        final String featureHolder,
                                                                        final TypeToken<SerializableMap<String, F>> featureType) throws IOException {
        final byte[] serializedForm = getValue(resourceConfig, featureHolder, byte[].class, ArrayUtils::emptyByteArray);
        return ArrayUtils.isNullOrEmpty(serializedForm) ?
                ImmutableMap.of() :
                IOUtils.deserialize(serializedForm, featureType, getClass().getClassLoader());
    }

    private Map<String, SerializableAttributeConfiguration> getAttributes(final Dictionary<String, ?> resourceConfig) throws IOException {
        return getFeatures(resourceConfig, ATTRIBUTES_PROPERTY, ATTRS_MAP_TYPE);
    }

    private Map<String, SerializableOperationConfiguration> getOperations(final Dictionary<String, ?> resourceConfig) throws IOException {
        return getFeatures(resourceConfig, OPERATIONS_PROPERTY, OPS_MAP_TYPE);
    }

    private Map<String, SerializableEventConfiguration> getEvents(final Dictionary<String, ?> resourceConfig) throws IOException {
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

    @Override
    void removeAll(final ConfigurationAdmin admin) throws IOException {
        try {
            forEachResource(admin, ALL_CONNECTORS_QUERY, Configuration::delete);
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    void fill(final ConfigurationAdmin admin,
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
        result.setType(getConnectorType(config.getFactoryPid()));
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
        configuration.put(RESOURCE_NAME_PROPERTY, resourceName);
        output.update(configuration);
    }

    private static Dictionary<String, Object> serialize(final SerializableManagedResourceConfiguration resource) throws IOException {
        final Dictionary<String, Object> result = new Hashtable<>(4);
        putValue(result, CONNECTION_STRING_PROPERTY, resource, SerializableManagedResourceConfiguration::getConnectionString);
        result.put(ATTRIBUTES_PROPERTY, IOUtils.serialize(resource.getAttributes()));
        result.put(EVENTS_PROPERTY, IOUtils.serialize(resource.getEvents()));
        result.put(OPERATIONS_PROPERTY, IOUtils.serialize(resource.getOperations()));
        //serialize properties
        resource.getParameters().forEach((name, value) -> {
            switch (name) {
                default:
                    result.put(name, value);
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
            }
        });
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
            final MutableBoolean updated = new MutableBoolean();
            //find existing configuration of resources
            forEachResource(admin, String.format("(%s=%s)", RESOURCE_NAME_PROPERTY, resourceName), config -> {
                serialize(resourceName, resource, config);
                updated.setTrue();
            });
            //no existing configuration, creates a new configuration
            if (!updated.get())
                serialize(resourceName, resource, admin.createFactoryConfiguration(getFactoryPersistentID(resource.getType()), null));
        } catch (final IOException | InvalidSyntaxException e) {
            throw new ManagedResourceConfigurationException(resourceName, e);
        }
    }

    @Override
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
        config.getEntities(SerializableManagedResourceConfiguration.class).modifiedEntries((resourceName, resource) -> {
            serialize(resourceName, resource, admin);
            return true;
        });
    }
}
