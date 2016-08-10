package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.snmp4j.log.OSGiLogFactory;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * Represents SNMP connector activator.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnmpResourceConnectorActivator extends ManagedResourceActivator<SnmpResourceConnector> {
    static {
        OSGiLogFactory.setup();
    }

    private static final class SnmpConnectorFactory extends ManagedResourceConnectorModeler<SnmpResourceConnector> {

        @Override
        public SnmpResourceConnector createConnector(final String resourceName,
                                                     final String connectionString,
                                                     final Map<String, String> connectionOptions,
                                                     final RequiredService<?>... dependencies) throws IOException {
            final SnmpResourceConnector result =
                    new SnmpResourceConnector(resourceName,
                            connectionString,
                            connectionOptions);
            result.listen();
            return result;
        }
        @Override
        protected boolean addAttribute(final SnmpResourceConnector connector, final String attributeName, final Duration readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeName, readWriteTimeout, options);
        }

        @Override
        protected void retainAttributes(final SnmpResourceConnector connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected boolean enableNotifications(final SnmpResourceConnector connector, final String category, final CompositeData options) {
            return connector.enableNotifications(category, options);
        }

        @Override
        protected void retainNotifications(final SnmpResourceConnector connector, final Set<String> events) {
            connector.disableNotificationsExcept(events);
        }

        @Override
        protected boolean enableOperation(final SnmpResourceConnector connector, final String operationName, final Duration timeout, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void retainOperations(final SnmpResourceConnector connector, final Set<String> operations) {
            //not supported
        }
    }

    @SpecialUse
    public SnmpResourceConnectorActivator() {
        super(new SnmpConnectorFactory(),
                new RequiredService<?>[]{new SimpleDependency<>(ThreadPoolRepository.class)},
                new SupportConnectorServiceManager<?, ?>[]{
                        configurationDescriptor(SnmpConnectorDescriptionProvider::getInstance),
                        discoveryService(SnmpResourceConnectorActivator::newDiscoveryService)
                });
    }

    private static SnmpDiscoveryService newDiscoveryService(final RequiredService<?>... dependencies){
        return new SnmpDiscoveryService();
    }
}
