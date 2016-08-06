package com.bytex.snamp.testing.connectors.snmp;

import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;

import java.util.Map;

/**
 * Represents an abstract class for SNMP connector tests.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.SNMP_CONNECTOR)
public abstract class AbstractSnmpConnectorTest extends AbstractResourceConnectorTest {
    protected static final String CONNECTOR_NAME = "snmp";

    protected AbstractSnmpConnectorTest(final String host,
                                        final int port,
                                        final Map<String, String> parameters){
        super(CONNECTOR_NAME, "udp:" + host + "/" + port, parameters);
    }
}
