package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.internal.CMManagedResourceParser;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.io.SerializableMap;
import com.google.common.reflect.TypeToken;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.configuration.impl.SerializableManagedResourceConfiguration.*;
import static com.bytex.snamp.connector.ManagedResourceConnector.CAPABILITY_NAMESPACE;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class DefaultManagedResourceParser extends AbstractTypedConfigurationParser<SerializableManagedResourceConfiguration> implements CMManagedResourceParser {
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

    private static final String ALL_CONNECTORS_QUERY = String.format("(%s=%s)", SERVICE_PID, String.format(CONNECTOR_PID_TEMPLATE, "*"));

    private static final LazySoftReference<DefaultManagedResourceParser> INSTANCE = new LazySoftReference<>();

    private DefaultManagedResourceParser() {
        super(RESOURCE_NAME_PROPERTY, SerializableAgentConfiguration::getResources);
    }

    static DefaultManagedResourceParser getInstance(){
        return INSTANCE.lazyGet(DefaultManagedResourceParser::new);
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

    @Override
    String getType(final Configuration config) {
        return CONNECTOR_PID_REPLACEMENT.matcher(config.getFactoryPid()).replaceFirst("");
    }

    private String getConnectionString(final Dictionary<String, ?> resourceConfig) {
        return getValue(resourceConfig, CONNECTION_STRING_PROPERTY, Objects::toString).orElse("");
    }

    private Map<String, SerializableAttributeConfiguration> getAttributes(final Dictionary<String, ?> resourceConfig) throws IOException {
        return deserialize(ATTRIBUTES_PROPERTY, ATTRS_MAP_TYPE, resourceConfig);
    }

    private Map<String, SerializableOperationConfiguration> getOperations(final Dictionary<String, ?> resourceConfig) throws IOException {
        return deserialize(OPERATIONS_PROPERTY, OPS_MAP_TYPE, resourceConfig);
    }

    private Map<String, SerializableEventConfiguration> getEvents(final Dictionary<String, ?> resourceConfig) throws IOException {
        return deserialize(EVENTS_PROPERTY, EVENTS_MAP_TYPE, resourceConfig);
    }

    @Override
    void removeAll(final ConfigurationAdmin admin) throws IOException {
        removeAll(admin, ALL_CONNECTORS_QUERY);
    }

    @Override
    void populateRepository(final ConfigurationAdmin admin,
                            final EntityMap<SerializableManagedResourceConfiguration> output) throws IOException {
        populateRepository(admin, ALL_CONNECTORS_QUERY, output);
    }

    @Override
    @Nonnull
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
        return createParserResult(configuration, result, CONNECTION_STRING_PROPERTY, ATTRIBUTES_PROPERTY, EVENTS_PROPERTY, OPERATIONS_PROPERTY);
    }

    @Override
    @Nonnull
    Dictionary<String, Object> serialize(final SerializableManagedResourceConfiguration resource) throws IOException {
        final Dictionary<String, Object> result = new Hashtable<>(4);
        result.put(CONNECTION_STRING_PROPERTY, resource.getConnectionString());
        result.put(ATTRIBUTES_PROPERTY, IOUtils.serialize(resource.getAttributes()));
        result.put(EVENTS_PROPERTY, IOUtils.serialize(resource.getEvents()));
        result.put(OPERATIONS_PROPERTY, IOUtils.serialize(resource.getOperations()));
        return result;
    }

    @Override
    void saveChanges(final SerializableEntityMap<SerializableManagedResourceConfiguration> resources,
                     final ConfigurationAdmin admin) throws IOException {
        saveChanges(resources, ALL_CONNECTORS_QUERY, admin);
    }
}
