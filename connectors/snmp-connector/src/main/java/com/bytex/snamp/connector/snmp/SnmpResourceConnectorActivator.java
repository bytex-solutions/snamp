package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.snmp4j.log.OSGiLogFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Duration;

import static com.bytex.snamp.MapUtils.getValueAsLong;


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
                new SupportServiceManager<?, ?>[]{
                        configurationDescriptor(SnmpConnectorDescriptionProvider::getInstance)
                });
    }

    private static Duration getDiscoveryTimeout(final AgentConfiguration configuration) {
        final long timeout = getValueAsLong(configuration,
                AgentConfiguration.DISCOVERY_TIMEOUT_PROPERTY,
                Long::parseLong).orElse(5000L);
        return Duration.ofMillis(timeout);
    }

    @Nonnull
    private static SnmpResourceConnector createConnector(final String resourceName,
                                                         final ManagedResourceInfo configuration,
                                                         final DependencyManager dependencies) throws IOException {
        final ConfigurationManager configManager = dependencies.getService(ConfigurationManager.class)
                .orElseThrow(AssertionError::new);

        final SnmpResourceConnector result =
                new SnmpResourceConnector(resourceName,
                        configuration,
                        configManager.transformConfiguration(SnmpResourceConnectorActivator::getDiscoveryTimeout));
        result.listen();
        return result;
    }
}
