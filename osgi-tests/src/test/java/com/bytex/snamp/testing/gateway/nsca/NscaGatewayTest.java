package com.bytex.snamp.testing.gateway.nsca;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
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

import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.NSCA_GATEWAY})
public final class NscaGatewayTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String GATEWAY_NAME = "nsca";
    private static final String INSTANCE_NAME = "test-nsca";
    private ServerSocket server;
    private static final int PORT = 9652;
    private Random rnd;

    public NscaGatewayTest() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
        rnd = new Random(42L);
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
        ConfigurationEntityDescription desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, GatewayConfiguration.class);
        testConfigurationDescriptor(desc, "nagiosHost", "nagiosPort", "connectionTimeout", "password", "encryption", "passiveCheckSendPeriod");
        desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, AttributeConfiguration.class);
        testConfigurationDescriptor(desc, "serviceName", "maxValue", "minValue", "units");
        desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, EventConfiguration.class);
        testConfigurationDescriptor(desc, "serviceName");
    }

    @Test
    public void attributeBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        try (final GatewayClient client = GatewayClient.tryCreate(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2))
                .orElseThrow(AssertionError::new)) {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo != null));
        }
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        gateways.addAndConsume(INSTANCE_NAME, nscaGateway -> {
            nscaGateway.setType(GATEWAY_NAME);
            nscaGateway.put("nagiosPort", Integer.toString(PORT));
            nscaGateway.put("nagiosHost", "localhost");
        });
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
        syncWithGatewayStartedEvent(GATEWAY_NAME, () -> {
                GatewayActivator.enableGateway(context, GATEWAY_NAME);
                return null;
        }, Duration.ofMinutes(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_NAME);
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
        attribute.setAlternativeName("int32");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("serviceName", "memory");
        attribute.put(DescriptorUtils.MAX_VALUE_FIELD, "100");
        attribute.put(DescriptorUtils.MIN_VALUE_FIELD, "0");
        attribute.put(DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD, "MB");
    }
}
