package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.BooleanBox;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.internal.CMManagedResourceParser;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.io.SerializableMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

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
    private static final Pattern CONNECTOR_PID_REPLACEMENT = Pattern.compile(String.format(CONNECTOR_PID_TEMPLATE, ""), Pattern.LITERAL);

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
        return CONNECTOR_PID_REPLACEMENT.matcher(factoryPID).replaceFirst("");
    }

    private String getConnectionString(final Dictionary<String, ?> resourceConfig) {
        return getValue(resourceConfig, CONNECTION_STRING_PROPERTY, Objects::toString).orElse("");
    }

    @Override
    public String getResourceName(final Dictionary<String, ?> resourceConfig) {
        return getValue(resourceConfig, RESOURCE_NAME_PROPERTY, Objects::toString).orElse("");
    }

    private <F extends FeatureConfiguration> Map<String, F> getFeatures(final Dictionary<String, ?> resourceConfig,
                                                                        final String featureHolder,
                                                                        final TypeToken<SerializableMap<String, F>> featureType) throws IOException {
        final byte[] serializedForm = getValue(resourceConfig, featureHolder, byte[].class).orElseGet(ArrayUtils::emptyByteArray);
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

    @Override
    void removeAll(final ConfigurationAdmin admin) throws IOException {
        removeAll(admin, ALL_CONNECTORS_QUERY);
    }

    private void fill(final Configuration config, final Map<String, SerializableManagedResourceConfiguration> output) throws IOException {
        final SingletonMap<String, SerializableManagedResourceConfiguration> resource;
        final Dictionary<String, ?> properties = config.getProperties();
        if (properties == null)
            return;
        else
            resource = parse(properties);
        resource.getValue().setType(getConnectorType(config.getFactoryPid()));
        resource.getValue().reset();
        output.putAll(resource);
    }

    @Override
    void fill(final ConfigurationAdmin admin,
                                      final Map<String, SerializableManagedResourceConfiguration> output) throws IOException {
        forEachConfiguration(admin, ALL_CONNECTORS_QUERY, config -> fill(config, output));
    }

    @Override
    public SingletonMap<String, SerializableManagedResourceConfiguration> parse(final Dictionary<String, ?> configuration) throws IOException {
        final SerializableManagedResourceConfiguration result = new SerializableManagedResourceConfiguration();
        result.setConnectionString(getConnectionString(configuration));
        //deserialize attributes
        result.setAttributes(getAttributes(configuration));
        //deserialize events
        result.setEvents(getEvents(configuration));
        //deserialize operations
        result.setOperations(getOperations(configuration));
        //deserialize parameters
        fillProperties(configuration, result, CONNECTION_STRING_PROPERTY, ATTRIBUTES_PROPERTY, EVENTS_PROPERTY, OPERATIONS_PROPERTY, RESOURCE_NAME_PROPERTY);
        result.reset();
        return new SingletonMap<>(getResourceName(configuration), result);
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
        resource.forEach((name, value) -> {
            if (!IGNORED_PROPERTIES.contains(name))
                result.put(name, value);
        });
        return result;
    }

    private void serialize(final String resourceName,
                             final SerializableManagedResourceConfiguration resource,
                             final ConfigurationAdmin admin) throws ManagedResourceConfigurationException {
        try {
            final BooleanBox updated = BoxFactory.createForBoolean(false);
            //find existing configuration of resources
            forEachConfiguration(admin, String.format("(%s=%s)", RESOURCE_NAME_PROPERTY, resourceName), config -> {
                serialize(resourceName, resource, config);
                updated.set(true);
            });
            //no existing configuration, creates a new configuration
            if (!updated.get())
                serialize(resourceName, resource, admin.createFactoryConfiguration(getFactoryPersistentID(resource.getType()), null));
        } catch (final IOException e) {
            throw new ManagedResourceConfigurationException(resourceName, e);
        }
    }

    @Override
    void saveChanges(final SerializableAgentConfiguration config,
                     final ConfigurationAdmin admin) throws IOException {
        //remove all unnecessary resources
        final Map<String, ? extends SerializableManagedResourceConfiguration> resources = config.getEntities(SerializableManagedResourceConfiguration.class);
        forEachConfiguration(admin, ALL_CONNECTORS_QUERY, output -> {
            final String connectorType = getConnectorType(output.getFactoryPid());
            final ManagedResourceConfiguration resourceConfig = resources.get(getResourceName(output.getProperties()));
            //delete resource if its type was changed
            if (resourceConfig == null || !Objects.equals(resourceConfig.getType(), connectorType))
                output.delete();
        });
        //save each modified resource
        config.getEntities(SerializableManagedResourceConfiguration.class).modifiedEntries((resourceName, resource) -> {
            serialize(resourceName, resource, admin);
            return true;
        });
    }
}
