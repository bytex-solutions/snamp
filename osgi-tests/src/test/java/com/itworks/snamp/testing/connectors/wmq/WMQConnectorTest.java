package com.itworks.snamp.testing.connectors.wmq;

import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.junit.Assume;
import org.junit.Test;

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

    @Test
    public void simpleTest() throws Exception {
        Assume.assumeTrue(isWmqInstalled());
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }
}
