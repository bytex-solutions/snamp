package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.connectors.AbstractManagedResourceActivator;

import java.util.Map;

/**
 * Represents SNMP connector activator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpResourceConnectorActivator extends AbstractManagedResourceActivator<SnmpResourceConnector> {

    private static final class SnmpManagedResourceConnectorProviderFactory extends ManagedResourceConnectorProviderFactory<SnmpResourceConnector> {

        /**
         * Creates a new instance of the management connector factory.
         *
         * @param resourceName         The name of the managed resource.
         * @param instances            Count of already instantiated connectors.
         * @param services             A collection of resolved dependencies.
         * @param activationProperties A collection of activation properties to read.
         * @return A new instance of the resource connector factory.
         */
        @Override
        protected ManagedResourceConnectorProvider<SnmpResourceConnector> createConnectorFactory(final String resourceName, final long instances, final Iterable<RequiredService<?>> services, final ActivationPropertyReader activationProperties) {
            return new ManagedResourceConnectorProvider<SnmpResourceConnector>(resourceName) {
                @Override
                protected SnmpResourceConnector createConnector(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws Exception {
                    return new SnmpResourceConnector(connectionString, connectionOptions);
                }
            };
        }
    }

    public SnmpResourceConnectorActivator(){
        super(SnmpConnectorHelpers.CONNECTOR_NAME, new SnmpManagedResourceConnectorProviderFactory());
    }


}
