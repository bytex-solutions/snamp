package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.log.OSGiLogFactory;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.util.DefaultThreadFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ExecutorService;


/**
 * Represents SNMP connector activator.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class SnmpResourceConnectorActivator extends ManagedResourceActivator {
    static {
        OSGiLogFactory.setup();
        SNMP4JSettings.setThreadJoinTimeout(4000L);
        SNMP4JSettings.setThreadFactory(new DefaultThreadFactory());
    }

    private static final class SnmpConnectorLifecycleManager extends DefaultManagedResourceLifecycleManager<SnmpResourceConnector> {
        @Nonnull
        @Override
        protected SnmpResourceConnector createConnector(final String resourceName,
                                                        final String connectionString,
                                                        final Map<String, String> configuration) throws Exception {
            final SnmpConnectorDescriptionProvider parser = SnmpConnectorDescriptionProvider.getInstance();
            final Address connectionAddress = GenericAddress.parse(connectionString);
            final OctetString engineID = parser.parseEngineID(configuration);
            final OctetString community = parser.parseCommunity(configuration);
            final ExecutorService threadPool = parser.parseThreadPool(configuration);
            final OctetString userName = parser.parseUserName(configuration);
            final OID authProtocol = parser.parseAuthProtocol(configuration);
            final OctetString password = parser.parsePassword(configuration);
            final OID encryptionProtocol = parser.parseEncryptionProtocol(configuration);
            final OctetString encryptionKey = parser.parseEncryptionKey(configuration);
            final Address localAddress = parser.parseLocalAddress(configuration);
            final OctetString securityContext = parser.parseSecurityContext(configuration);
            final int socketTimeout = parser.parseSocketTimeout(configuration);
            final SnmpClient client = userName == null || password == null || encryptionKey == null ?
                    SnmpClient.create(connectionAddress, community, localAddress, socketTimeout, threadPool) :
                    SnmpClient.create(connectionAddress, engineID, userName, authProtocol, password, encryptionProtocol, encryptionKey, securityContext, localAddress, socketTimeout, threadPool);
            final SnmpResourceConnector result = new SnmpResourceConnector(resourceName,
                    client,
                    parser.parseDiscoveryTimeout(configuration));
            client.listen();
            return result;
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public SnmpResourceConnectorActivator() {
        super(new SnmpConnectorLifecycleManager(),
                configurationDescriptor(SnmpConnectorDescriptionProvider::getInstance));
    }
}
