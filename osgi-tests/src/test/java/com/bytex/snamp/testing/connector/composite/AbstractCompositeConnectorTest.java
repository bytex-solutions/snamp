package com.bytex.snamp.testing.connector.composite;

import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import org.osgi.framework.BundleContext;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies(SnampFeature.COMPOSITE_CONNECTOR)
public abstract class AbstractCompositeConnectorTest extends AbstractResourceConnectorTest {
    static final String CONNECTOR_NAME = "composite";

    protected AbstractCompositeConnectorTest(final String connectionString, final Map<String, String> parameters) {
        super(CONNECTOR_NAME, connectionString, parameters);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }
}
