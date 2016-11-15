package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies(SnampFeature.HTTP_ACCEPTOR)
public abstract class AbstractHttpConnectorTest extends AbstractResourceConnectorTest {
    public static final String CONNECTOR_TYPE = "http";

    protected AbstractHttpConnectorTest(final String instanceName){
        super(CONNECTOR_TYPE, instanceName);
    }
}
