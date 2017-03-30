package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.internal.CMGatewayParser;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.regex.Pattern;

import static com.bytex.snamp.gateway.Gateway.CAPABILITY_NAMESPACE;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMGatewayParserImpl extends AbstractTypedConfigurationParser<SerializableGatewayConfiguration> implements CMGatewayParser {
    private static final String GATEWAY_PID_TEMPLATE = CAPABILITY_NAMESPACE + ".%s";
    private static final String GATEWAY_INSTANCE_NAME_PROPERTY = "$gatewayInstanceName$";
    private static final String ALL_GATEWAYS_QUERY = String.format("(%s=%s)", SERVICE_PID, String.format(GATEWAY_PID_TEMPLATE, "*"));
    private static final Pattern GATEWAY_PID_REPLACEMENT = Pattern.compile(String.format(GATEWAY_PID_TEMPLATE, ""), Pattern.LITERAL);

    CMGatewayParserImpl() {
        super(GATEWAY_INSTANCE_NAME_PROPERTY, SerializableAgentConfiguration::getGateways);
    }

    @Override
    String getType(final Configuration config) {
        return GATEWAY_PID_REPLACEMENT.matcher(config.getFactoryPid()).replaceFirst("");
    }

    @Override
    public String getFactoryPersistentID(final String gatewayType) {
        return String.format(GATEWAY_PID_TEMPLATE, gatewayType);
    }

    @Override
    public String getInstanceName(final Dictionary<String, ?> gatewayInstanceConfig) {
        return getIdentityName(gatewayInstanceConfig);
    }

    @Override
    void populateRepository(final ConfigurationAdmin admin,
                            final EntityMap<SerializableGatewayConfiguration> output) throws IOException {
        populateRepository(admin, ALL_GATEWAYS_QUERY, output);
    }

    @Override
    void removeAll(final ConfigurationAdmin admin) throws IOException {
        removeAll(admin, ALL_GATEWAYS_QUERY);
    }

    @Override
    public SingletonMap<String, SerializableGatewayConfiguration> parse(final Dictionary<String, ?> configuration) {
        return createParserResult(configuration, new SerializableGatewayConfiguration());
    }

    @Override
    Dictionary<String, Object> serialize(final SerializableGatewayConfiguration gatewayInstance) {
        final Dictionary<String, Object> result = new Hashtable<>(4);
        gatewayInstance.forEach((name, value) -> {
            if (!IGNORED_PROPERTIES.contains(name))
                result.put(name, value);
        });
        return result;
    }

    @Override
    void saveChanges(final SerializableEntityMap<SerializableGatewayConfiguration> instances,
              final ConfigurationAdmin admin) throws IOException {
        saveChanges(instances, ALL_GATEWAYS_QUERY, admin);
    }
}
