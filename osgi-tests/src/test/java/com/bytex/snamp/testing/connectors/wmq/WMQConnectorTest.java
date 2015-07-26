package com.bytex.snamp.testing.connectors.wmq;

import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.util.Locale;
import java.util.concurrent.Future;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.WMQ_CONNECTOR)
public final class WMQConnectorTest extends AbstractResourceConnectorTest {
    private static final String CONNECTOR_NAME = "ibm-wmq";

    public WMQConnectorTest() {
        super(CONNECTOR_NAME, "");
    }

    private boolean isWmqInstalled() throws Exception {
        final Future<String> installed = ManagedResourceConnectorClient.invokeMaintenanceAction(getTestBundleContext(),
                CONNECTOR_NAME,
                "isWmqInstalled",
                "",
                Locale.getDefault());
        return Boolean.valueOf(installed.get());
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        if (isWmqInstalled())
            super.afterStartTest(context);
    }

    @Test
    public void simpleTest() throws Exception {
        Assume.assumeTrue("WebSphere MQ classes for Java are not installed", isWmqInstalled());
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }
}
