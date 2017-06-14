package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.snmp4j.log.OSGiLogFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.bytex.snamp.ArrayUtils.toArray;


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

    @SpecialUse(SpecialUse.Case.OSGi)
    public SnmpResourceConnectorActivator() {
        super(SnmpResourceConnectorActivator::createConnector,
                requiredBy(SnmpResourceConnector.class).require(ThreadPoolRepository.class),
                toArray(configurationDescriptor(SnmpConnectorDescriptionProvider::getInstance)));
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
