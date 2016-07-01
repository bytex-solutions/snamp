package com.bytex.snamp.testing.adapters.nsca;

import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import com.google.common.primitives.Ints;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@SnampDependencies({SnampFeature.NSCA_ADAPTER})
public final class NscaAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "nsca";
    private static final String INSTANCE_NAME = "test-nsca";
    private ServerSocket server;
    private static final int PORT = 9652;
    private Random rnd;

    public NscaAdapterTest() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
        rnd = new Random(42L);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void testIntAttribute() throws JMException, IOException {
        final ManagedResourceConnector connector = getManagementConnector();
        try{
            connector.setAttribute(new Attribute("3.0", 80));
            try(final Socket socket = server.accept();
            final OutputStream out = socket.getOutputStream()){
                final byte[] initializationVector = new byte[128];
                rnd.nextBytes(initializationVector);
                out.write(initializationVector);
                out.write(Ints.toByteArray(65343));//timestamp required
                final byte[] content = IOUtils.readFully(socket.getInputStream());
                assertNotNull(content);
                assertTrue(content.length > 0);
            }
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        testConfigurationDescriptor(desc, "nagiosHost", "nagiosPort", "connectionTimeout", "password", "encryption", "passiveCheckSendPeriod");
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, AttributeConfiguration.class);
        testConfigurationDescriptor(desc, "serviceName", "maxValue", "minValue", "units");
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, EventConfiguration.class);
        testConfigurationDescriptor(desc, "serviceName");
    }

    @Test
    public void attributeBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo != null));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        final ResourceAdapterConfiguration nscaAdapter = adapters.getOrAdd(INSTANCE_NAME);
        nscaAdapter.setAdapterName(ADAPTER_NAME);
        nscaAdapter.getParameters().put("nagiosPort", Integer.toString(PORT));
        nscaAdapter.getParameters().put("nagiosHost", "localhost");
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        server = new ServerSocket(PORT);
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithAdapterStartedEvent(ADAPTER_NAME, (BundleExceptionCallable) () -> {
                ResourceAdapterActivator.startResourceAdapter(context, ADAPTER_NAME);
                return null;
        }, Duration.ofMinutes(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        try {
            super.afterCleanupTest(context);
        }
        finally {
            server.close();
            server = null;
        }
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("3.0");
        setFeatureName(attribute, "int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("serviceName", "memory");
        attribute.getParameters().put(DescriptorUtils.MAX_VALUE_FIELD, "100");
        attribute.getParameters().put(DescriptorUtils.MIN_VALUE_FIELD, "0");
        attribute.getParameters().put(DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD, "MB");
    }
}
