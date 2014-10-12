package com.itworks.snamp.testing.adapters.ssh;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import com.itworks.snamp.testing.connectors.rshell.AbstractRShellConnectorTest;
import org.apache.commons.collections4.Factory;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Represents SSH-to-RShell integration test.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxToSshTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;
    private static final String FINGERPRINT = "e8:0d:af:84:bb:ec:05:03:b9:7c:f3:75:19:5a:2a:63";
    private static final String CERTIFICATE_FILE = "hostkey.ser";
    private static final String ADAPTER_NAME = "ssh";

    public JmxToSshTest() {
        super(USER_NAME,
                PASSWORD,
                PORT,
                CERTIFICATE_FILE,
                FINGERPRINT,
                SnampArtifact.SSH_ADAPTER.getReference(),
                mavenBundle("net.engio", "mbassador", "1.1.10"));
    }

    @Test
    public void simpleHostTest() throws InterruptedException {
        Thread.sleep(10000000);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        super.afterStartTest(context);
        AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        AbstractResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Factory<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration restAdapter = adapterFactory.create();
        restAdapter.setAdapterName(ADAPTER_NAME);
        restAdapter.getHostingParams().put("port", "34000");
        restAdapter.getHostingParams().put("userName", USER_NAME);
        restAdapter.getHostingParams().put("password", PASSWORD);
        adapters.put("test-jmx", restAdapter);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Factory<AttributeConfiguration> attributeFactory) {
        final AttributeConfiguration attr = attributeFactory.create();
        attr.setAttributeName("memStatus");
        attr.getParameters().put("commandProfileLocation", "freemem-tool-profile.xml");
        attr.getParameters().put("format", "-m");
        attributes.put("ms", attr);
    }
}
