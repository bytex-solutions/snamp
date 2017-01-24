package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.BooleanBox;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.internal.CMGatewayParser;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.gateway.Gateway.CAPABILITY_NAMESPACE;
import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMGatewayParserImpl extends AbstractConfigurationParser<SerializableGatewayConfiguration> implements CMGatewayParser {
    private static final String GATEWAY_PID_TEMPLATE = CAPABILITY_NAMESPACE + ".%s";
    private static final String GATEWAY_INSTANCE_NAME_PROPERTY = "$gatewayInstanceName$";
    private static final String ALL_GATEWAYS_QUERY = String.format("(%s=%s)", Constants.SERVICE_PID, String.format(GATEWAY_PID_TEMPLATE, "*"));

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
        return factoryPID.replaceFirst(String.format(GATEWAY_PID_TEMPLATE, ""), "");
    }

    @Override
    public String getFactoryPersistentID(final String gatewayType) {
        return String.format(GATEWAY_PID_TEMPLATE, gatewayType);
    }

    @Override
    public String getInstanceName(final Dictionary<String, ?> gatewayInstanceConfig) {
        return getValue(gatewayInstanceConfig, GATEWAY_INSTANCE_NAME_PROPERTY, Objects::toString).orElse("");
    }

    private static void fillGatewayInstanceParameters(final Dictionary<String, ?> instanceConfig,
                                                      final Map<String, String> output){
        final Enumeration<String> names = instanceConfig.keys();
        while (names.hasMoreElements()){
            final String name = names.nextElement();
            switch (name){
                default:
                    final Object value = instanceConfig.get(name);
                    if(value != null)
                        output.put(name, value.toString());
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
                case GATEWAY_INSTANCE_NAME_PROPERTY:
            }
        }
    }

    private static <E extends Exception> void forEachGatewayInstance(final ConfigurationAdmin admin,
                                                                     final String filter,
                                                                     final Acceptor<Configuration, E> reader) throws E, IOException, InvalidSyntaxException {
        final Configuration[] configs = admin.listConfigurations(filter);
        if(configs != null && configs.length > 0)
            for(final Configuration config: configs)
                reader.accept(config);
    }

    @Override
    void fill(final ConfigurationAdmin admin,
                                     final Map<String, SerializableGatewayConfiguration> output) throws IOException {
        callAndWrapException(() -> {
            forEachGatewayInstance(admin, ALL_GATEWAYS_QUERY, config -> {
                final String gatewayInstanceName = getInstanceName(config.getProperties());
                final SerializableGatewayConfiguration gatewayInstance = parse(config);
                output.put(gatewayInstanceName, gatewayInstance);
            });
            return null;
        }, IOException::new);
    }

    @Override
    void removeAll(final ConfigurationAdmin admin) throws IOException {
        callAndWrapException(() -> {
            forEachGatewayInstance(admin, ALL_GATEWAYS_QUERY, Configuration::delete);
            return null;
        }, IOException::new);
    }

    @Override
    public SerializableGatewayConfiguration parse(final Dictionary<String, ?> configuration) {
        final SerializableGatewayConfiguration result = new SerializableGatewayConfiguration();
        //deserialize parameters
        fillGatewayInstanceParameters(configuration, result);
        result.reset();
        return result;
    }

    @Override
    public SerializableGatewayConfiguration parse(final Configuration config) {
        final SerializableGatewayConfiguration result = parse(config.getProperties());
        result.setType(getGatewayType(config.getFactoryPid()));
        result.reset();
        return result;
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

        for(final Map.Entry<String, String> entry: gatewayInstance.entrySet())
            switch (entry.getKey()) {
                default: result.put(entry.getKey(), entry.getValue());
                case Constants.SERVICE_PID:
                case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
                case ConfigurationAdmin.SERVICE_FACTORYPID:
            }
        return result;
    }

    @Override
    public void serialize(final GatewayConfiguration input, final Configuration output) throws IOException {
        assert input instanceof SerializableGatewayConfiguration;
        output.update(serialize((SerializableGatewayConfiguration) input));
    }

    private void serialize(final String gatewayInstanceName,
                             final SerializableGatewayConfiguration gatewayInstance,
                             final ConfigurationAdmin admin) throws GatewayConfigurationException {
        try {
            //find existing configuration of gateway
            final BooleanBox updated = BoxFactory.createForBoolean(false);
            forEachGatewayInstance(admin, String.format("(%s=%s)", GATEWAY_INSTANCE_NAME_PROPERTY, gatewayInstanceName), config -> {
                serialize(gatewayInstanceName, gatewayInstance, config);
                updated.set(true);
            });
            //no existing configuration, creates a new configuration
            if (!updated.get())
                serialize(gatewayInstanceName,
                        gatewayInstance,
                        admin.createFactoryConfiguration(getFactoryPersistentID(gatewayInstance.getType()), null));
        } catch (final IOException | InvalidSyntaxException e) {
            throw new GatewayConfigurationException(gatewayInstanceName, e);
        }
    }

    @Override
    void saveChanges(final SerializableAgentConfiguration config,
              final ConfigurationAdmin admin) throws IOException {
        final ConfigurationEntityList<? extends SerializableGatewayConfiguration> instances = config.getEntities(SerializableGatewayConfiguration.class);
        //remove all unnecessary gateway
        callAndWrapException(() -> {
            forEachGatewayInstance(admin, ALL_GATEWAYS_QUERY, output -> {
                final String gatewayType = getGatewayType(output.getFactoryPid());
                final GatewayConfiguration gateway = instances.get(getInstanceName(output.getProperties()));
                //delete resource if its type was changed
                if (gateway == null || !Objects.equals(gatewayType, gateway.getType()))
                    output.delete();
            });
            return null;
        }, IOException::new);
        //save each modified gateway instance
        config.getEntities(SerializableGatewayConfiguration.class).modifiedEntries((gatewayInstanceName, gatewayInstanceConfig) -> {
            serialize(gatewayInstanceName, gatewayInstanceConfig, admin);
            return true;
        });
    }
}
