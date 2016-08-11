package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Box;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.internal.CMGatewayParser;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Maps;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMGatewayParserImpl extends AbstractConfigurationParser<SerializableGatewayConfiguration> implements CMGatewayParser {
    private static final String GATEWAY_PID_TEMPLATE = "com.bytex.snamp.gateway.%s";
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
        return Utils.getProperty(gatewayInstanceConfig, GATEWAY_INSTANCE_NAME_PROPERTY, String.class, "");
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

    @Override
    public Map<String, String> getParameters(final Dictionary<String, ?> gatewayInstanceConfig) {
        final Map<String, String> result = Maps.newHashMapWithExpectedSize(gatewayInstanceConfig.size());
        fillGatewayInstanceParameters(gatewayInstanceConfig, result);
        return result;
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
        try {
            forEachGatewayInstance(admin, ALL_GATEWAYS_QUERY, config -> {
                final String gatewayInstanceName = getInstanceName(config.getProperties());
                final SerializableGatewayConfiguration gatewayInstance = parse(config);
                output.put(gatewayInstanceName, gatewayInstance);
            });
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    void removeAll(final ConfigurationAdmin admin) throws IOException {
        try {
            forEachGatewayInstance(admin, ALL_GATEWAYS_QUERY, Configuration::delete);
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public SerializableGatewayConfiguration parse(final Configuration config) {
        final SerializableGatewayConfiguration result = new SerializableGatewayConfiguration();
        result.setType(getGatewayType(config.getFactoryPid()));
        //deserialize parameters
        fillGatewayInstanceParameters(config.getProperties(), result.getParameters());
        result.reset();
        return result;
    }

    private static void serialize(final String gatewayInstanceName,
                                  final SerializableGatewayConfiguration gatewayInstance,
                                  final Configuration output) throws IOException{
        final Dictionary<String, String> configuration = serialize(gatewayInstance);
        Utils.setProperty(configuration, GATEWAY_INSTANCE_NAME_PROPERTY, gatewayInstanceName);
        output.update(configuration);
    }

    private static Dictionary<String, String> serialize(final SerializableGatewayConfiguration gatewayInstance) {
        final Dictionary<String, String> result = new Hashtable<>(4);

        for(final Map.Entry<String, String> entry: gatewayInstance.getParameters().entrySet())
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
            final Box<Boolean> updated = new Box<>(Boolean.FALSE);
            forEachGatewayInstance(admin, String.format("(%s=%s)", GATEWAY_INSTANCE_NAME_PROPERTY, gatewayInstanceName), config -> {
                serialize(gatewayInstanceName, gatewayInstance, config);
                updated.set(Boolean.TRUE);
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
        final ConfigurationEntityRegistry<? extends SerializableGatewayConfiguration> instances = config.getEntities(SerializableGatewayConfiguration.class);
        //remove all unnecessary gateway
        try {
            forEachGatewayInstance(admin, ALL_GATEWAYS_QUERY, output -> {
                final String gatewayInstance = getInstanceName(output.getProperties());
                if (!instances.containsKey(gatewayInstance))
                    output.delete();
            });
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
        //save each modified gateway instance
        config.getEntities(SerializableGatewayConfiguration.class).modifiedEntries((gatewayInstanceName, gatewayInstanceConfig) -> {
            serialize(gatewayInstanceName, gatewayInstanceConfig, admin);
            return true;
        });
    }
}
