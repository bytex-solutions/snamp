package com.itworks.snamp.testing.connectors.snmp;

import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.ops4j.pax.exam.options.AbstractProvisionOption;

import java.util.Map;

/**
 * Represents an abstract class for SNMP connector tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSnmpConnectorTest extends AbstractResourceConnectorTest {
    protected static final String CONNECTOR_NAME = "snmp";

    protected AbstractSnmpConnectorTest(final String host,
                                        final int port,
                                        final Map<String, String> parameters,
                                        final AbstractProvisionOption<?>... deps){
        super(CONNECTOR_NAME, "udp:" + host + "/" + port, parameters, concat(deps, SnampArtifact.SNMP_CONNECTOR.getReference(), SnampArtifact.SNMP4J.getReference()));
    }
}
