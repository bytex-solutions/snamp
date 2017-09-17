package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.internal.CMSupervisorParser;
import com.bytex.snamp.io.IOUtils;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.regex.Pattern;

import static com.bytex.snamp.supervision.Supervisor.CAPABILITY_NAMESPACE;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class DefaultSupervisorParser extends AbstractTypedConfigurationParser<SerializableSupervisorConfiguration> implements CMSupervisorParser {
    private static final String SUPERVISOR_PID_TEMPLATE = CAPABILITY_NAMESPACE + ".%s";
    private static final String GROUP_NAME_PROPERTY = "$groupName$";
    private static final String HEALTH_CHECK_PROPERTY = "$healthCheck$";
    private static final String DISCOVERY_PROPERTY = "$discoveryConfig$";
    private static final String AUTO_SCALING_PROPERTY = "$autoScaling$";
    private static final String ALL_SUPERVISORS_QUERY = String.format("(%s=%s)", SERVICE_PID, String.format(SUPERVISOR_PID_TEMPLATE, "*"));
    private static final Pattern SUPERVISOR_PID_REPLACEMENT = Pattern.compile(String.format(SUPERVISOR_PID_TEMPLATE, ""), Pattern.LITERAL);

    private static final LazyReference<DefaultSupervisorParser> INSTANCE = LazyReference.soft();

    private DefaultSupervisorParser() {
        super(GROUP_NAME_PROPERTY, SerializableAgentConfiguration::getSupervisors);
    }

    static DefaultSupervisorParser getInstance(){
        return INSTANCE.get(DefaultSupervisorParser::new);
    }

    @Override
    String getType(final Configuration config) {
        return SUPERVISOR_PID_REPLACEMENT.matcher(config.getFactoryPid()).replaceFirst("");
    }

    @Override
    public String getFactoryPersistentID(final String supervisorType) {
        return String.format(SUPERVISOR_PID_TEMPLATE, supervisorType);
    }

    @Override
    @Nonnull
    Dictionary<String, Object> serialize(final SerializableSupervisorConfiguration entity) throws IOException {
        final Dictionary<String, Object> result = new Hashtable<>(4);
        result.put(HEALTH_CHECK_PROPERTY, IOUtils.serialize(entity.getHealthCheckConfig()));
        result.put(DISCOVERY_PROPERTY, IOUtils.serialize(entity.getDiscoveryConfig()));
        result.put(AUTO_SCALING_PROPERTY, IOUtils.serialize(entity.getAutoScalingConfig()));
        return result;
    }

    @Override
    void removeAll(final ConfigurationAdmin admin) throws IOException {
        removeAll(admin, ALL_SUPERVISORS_QUERY);
    }

    @Override
    void populateRepository(final ConfigurationAdmin source, final EntityMap<SerializableSupervisorConfiguration> dest) throws IOException {
        populateRepository(source, ALL_SUPERVISORS_QUERY, dest);
    }

    @Override
    void saveChanges(final SerializableEntityMap<SerializableSupervisorConfiguration> source, final ConfigurationAdmin dest) throws IOException {
        saveChanges(source, ALL_SUPERVISORS_QUERY, dest);
    }

    @Override
    @Nonnull
    public SingletonMap<String, SerializableSupervisorConfiguration> parse(final Dictionary<String, ?> configuration) throws IOException {
        final SerializableSupervisorConfiguration supervisor = new SerializableSupervisorConfiguration();
        supervisor.setHealthCheckConfig(deserialize(HEALTH_CHECK_PROPERTY, SerializableSupervisorConfiguration.SerializableHealthCheckConfiguration.class, configuration));
        supervisor.setDiscoveryConfig(deserialize(DISCOVERY_PROPERTY, SerializableSupervisorConfiguration.SerializableDiscoveryConfiguration.class, configuration));
        supervisor.setAutoScalingConfig(deserialize(AUTO_SCALING_PROPERTY, SerializableSupervisorConfiguration.SerializableAutoScalingConfiguration.class, configuration));
        return createParserResult(configuration, supervisor, HEALTH_CHECK_PROPERTY, DISCOVERY_PROPERTY, AUTO_SCALING_PROPERTY);
    }
}
