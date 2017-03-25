package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.BooleanBox;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.internal.CMGatewayParser;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.gateway.Gateway.CAPABILITY_NAMESPACE;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMGatewayParserImpl extends AbstractConfigurationParser<SerializableGatewayConfiguration> implements CMGatewayParser {
    private static final String GATEWAY_PID_TEMPLATE = CAPABILITY_NAMESPACE + ".%s";
    private static final String GATEWAY_INSTANCE_NAME_PROPERTY = "$gatewayInstanceName$";
    private static final String ALL_GATEWAYS_QUERY = String.format("(%s=%s)", SERVICE_PID, String.format(GATEWAY_PID_TEMPLATE, "*"));
    private static final Pattern GATEWAY_PID_REPLACEMENT = Pattern.compile(String.format(GATEWAY_PID_TEMPLATE, ""), Pattern.LITERAL);

    private static final class GatewayConfigurationException extends PersistentConfigurationException{
        private static final long serialVersionUID = -242953184038600223L;

        private GatewayConfigurationException(final String pid, final Throwable e) {
            super(pid, SerializableGatewayConfiguration.class, e);
        }
    }

    /**
     * Returns type of the gateway by its persistent identifier.
     * @param factoryPID Gateway persistent identifier.
     * @return Type of gateway.
     */
    private static String getGatewayType(final String factoryPID){
        return GATEWAY_PID_REPLACEMENT.matcher(factoryPID).replaceFirst("");
    }

    @Override
    public String getFactoryPersistentID(final String gatewayType) {
        return String.format(GATEWAY_PID_TEMPLATE, gatewayType);
    }

    @Override
    public String getInstanceName(final Dictionary<String, ?> gatewayInstanceConfig) {
        return getValue(gatewayInstanceConfig, GATEWAY_INSTANCE_NAME_PROPERTY, Objects::toString).orElse("");
    }

    private void fill(final Configuration config, final Map<String, SerializableGatewayConfiguration> output) throws IOException {
        final Dictionary<String, ?> properties = config.getProperties();
        final SingletonMap<String, SerializableGatewayConfiguration> gatewayInstance;
        if (properties == null)
            return;
        else
            gatewayInstance = parse(properties);
        gatewayInstance.getValue().setType(getGatewayType(config.getFactoryPid()));
        gatewayInstance.getValue().reset();
        output.putAll(gatewayInstance);
    }

    @Override
    void fill(final ConfigurationAdmin admin,
                                     final Map<String, SerializableGatewayConfiguration> output) throws IOException {
        forEachConfiguration(admin, ALL_GATEWAYS_QUERY, config -> fill(config, output));
    }

    @Override
    void removeAll(final ConfigurationAdmin admin) throws IOException {
        removeAll(admin, ALL_GATEWAYS_QUERY);
    }

    @Override
    public SingletonMap<String, SerializableGatewayConfiguration> parse(final Dictionary<String, ?> configuration) {
        final SerializableGatewayConfiguration result = new SerializableGatewayConfiguration();
        //deserialize parameters
        fillProperties(configuration, result, GATEWAY_INSTANCE_NAME_PROPERTY);
        result.reset();
        return new SingletonMap<>(getInstanceName(configuration), result);
    }

    private static void serialize(final String gatewayInstanceName,
                                  final SerializableGatewayConfiguration gatewayInstance,
                                  final Configuration output) throws IOException{
        final Dictionary<String, String> configuration = serialize(gatewayInstance);
        configuration.put(GATEWAY_INSTANCE_NAME_PROPERTY, gatewayInstanceName);
        output.update(configuration);
    }

    private static Dictionary<String, String> serialize(final SerializableGatewayConfiguration gatewayInstance) {
        final Dictionary<String, String> result = new Hashtable<>(4);
        gatewayInstance.forEach((name, value) -> {
            if (!IGNORED_PROPERTIES.contains(name))
                result.put(name, value);
        });
        return result;
    }

    private void serialize(final String gatewayInstanceName,
                             final SerializableGatewayConfiguration gatewayInstance,
                             final ConfigurationAdmin admin) throws GatewayConfigurationException {
        try {
            //find existing configuration of gateway
            final BooleanBox updated = BoxFactory.createForBoolean(false);
            forEachConfiguration(admin, String.format("(%s=%s)", GATEWAY_INSTANCE_NAME_PROPERTY, gatewayInstanceName), config -> {
                serialize(gatewayInstanceName, gatewayInstance, config);
                updated.set(true);
            });
            //no existing configuration, creates a new configuration
            if (!updated.get())
                serialize(gatewayInstanceName,
                        gatewayInstance,
                        admin.createFactoryConfiguration(getFactoryPersistentID(gatewayInstance.getType()), null));
        } catch (final IOException e) {
            throw new GatewayConfigurationException(gatewayInstanceName, e);
        }
    }

    @Override
    void saveChanges(final SerializableAgentConfiguration config,
              final ConfigurationAdmin admin) throws IOException {
        final ConfigurationEntityList<? extends SerializableGatewayConfiguration> instances = config.getEntities(SerializableGatewayConfiguration.class);
        //remove all unnecessary gateway
        forEachConfiguration(admin, ALL_GATEWAYS_QUERY, output -> {
            final String gatewayType = getGatewayType(output.getFactoryPid());
            final GatewayConfiguration gateway = instances.get(getInstanceName(output.getProperties()));
            //delete resource if its type was changed
            if (gateway == null || !Objects.equals(gatewayType, gateway.getType()))
                output.delete();
        });
        //save each modified gateway instance
        config.getEntities(SerializableGatewayConfiguration.class).modifiedEntries((gatewayInstanceName, gatewayInstanceConfig) -> {
            serialize(gatewayInstanceName, gatewayInstanceConfig, admin);
            return true;
        });
    }
}
