package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies(SnampFeature.HTTP_ACCEPTOR)
public abstract class AbstractHttpConnectorTest extends AbstractResourceConnectorTest {
    public static final String CONNECTOR_TYPE = "http";
    protected static final String COMPONENT_NAME = "javaApp";

    protected AbstractHttpConnectorTest(final String instanceName){
        super(CONNECTOR_TYPE, instanceName, ImmutableMap.of("componentName", COMPONENT_NAME));
    }
}
