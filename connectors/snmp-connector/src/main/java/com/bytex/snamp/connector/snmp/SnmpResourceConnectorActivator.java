package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.log.OSGiLogFactory;
import org.snmp4j.util.DefaultThreadFactory;

import javax.annotation.Nonnull;
import java.io.IOException;


/**
 * Represents SNMP connector activator.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class SnmpResourceConnectorActivator extends ManagedResourceActivator<SnmpResourceConnector> {
    static {
        OSGiLogFactory.setup();
        SNMP4JSettings.setThreadJoinTimeout(4000L);
        SNMP4JSettings.setThreadFactory(new DefaultThreadFactory());
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public SnmpResourceConnectorActivator() {
        super(SnmpResourceConnectorActivator::createConnector,
                configurationDescriptor(SnmpConnectorDescriptionProvider::getInstance),
                requiredBy(SnmpResourceConnector.class).require(ThreadPoolRepository.class));
    }

    @Nonnull
    private static SnmpResourceConnector createConnector(final String resourceName,
                                                         final ManagedResourceInfo configuration,
                                                         final DependencyManager dependencies) throws IOException {
        final SnmpResourceConnector result = new SnmpResourceConnector(resourceName, configuration);
        result.listen();
        return result;
    }
}
