package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.internal.CMSupervisorParser;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.regex.Pattern;

import static com.bytex.snamp.connector.supervision.ManagedResourceGroupSupervisor.CAPABILITY_NAMESPACE;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CMSupervisorParserImpl extends AbstractTypedConfigurationParser<SerializableSupervisorConfiguration> implements CMSupervisorParser {
    private static final String SUPERVISOR_PID_TEMPLATE = CAPABILITY_NAMESPACE + ".%s";
    private static final String GROUP_NAME_PROPERTY = "$groupName$";
    private static final String ALL_SUPERVISORS_QUERY = String.format("(%s=%s)", SERVICE_PID, String.format(SUPERVISOR_PID_TEMPLATE, "*"));
    private static final Pattern SUPERVISOR_PID_REPLACEMENT = Pattern.compile(String.format(SUPERVISOR_PID_TEMPLATE, ""), Pattern.LITERAL);

    CMSupervisorParserImpl() {
        super(GROUP_NAME_PROPERTY, SerializableAgentConfiguration::getSupervisors);
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
    Dictionary<String, Object> serialize(final SerializableSupervisorConfiguration entity) throws IOException {
        return null;
    }

    @Override
    public String getGroupName(final Dictionary<String, ?> configuration) {
        return getIdentityName(configuration);
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
    public SingletonMap<String, SerializableSupervisorConfiguration> parse(final Dictionary<String, ?> configuration) throws IOException {
        return null;
    }
}
