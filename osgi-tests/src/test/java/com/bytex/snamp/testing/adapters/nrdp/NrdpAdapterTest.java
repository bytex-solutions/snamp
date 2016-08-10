package com.bytex.snamp.testing.adapters.nrdp;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bytex.snamp.configuration.EntityMap;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

import com.bytex.snamp.configuration.ResourceAdapterConfiguration;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.NRDP_ADAPTER)
public final class NrdpAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "nrdp";
    private static final String INSTANCE_NAME = "test-nrdp";
    private static final int PORT = 9652;
    private HttpServer server;

    public NrdpAdapterTest() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    private static final class Mailbox extends CompletableFuture<Boolean> implements HttpHandler{

        private Mailbox(){
        }

        @Override
        public void handle(final HttpExchange exchange) throws IOException {
            final byte[] content = IOUtils.readFully(exchange.getRequestBody());
            assertTrue(content != null);
            assertTrue(content.length > 0);
            exchange.close();
            complete(true);
        }
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void testIntAttribute() throws Exception {
        final ManagedResourceConnector connector = getManagementConnector();
        try{
            connector.setAttribute(new Attribute("3.0", 80));
            final Mailbox listener = new Mailbox();
            final HttpContext context = server.createContext("/context", listener);
            try{
                listener.get(2, TimeUnit.SECONDS);
            }
            finally {
                server.removeContext(context);
            }
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        ConfigurationEntityDescription desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        testConfigurationDescriptor(desc, "serverURL", "connectionTimeout", "token", "passiveCheckSendPeriod");
        desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, AttributeConfiguration.class);
        testConfigurationDescriptor(desc, "serviceName", "maxValue", "minValue", "units");
        desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, EventConfiguration.class);
        testConfigurationDescriptor(desc, "serviceName");
    }

    @Test
    public void attributeBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final GatewayClient client = new GatewayClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo != null));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        final ResourceAdapterConfiguration nrdpAdapter = adapters.getOrAdd(INSTANCE_NAME);
        nrdpAdapter.setAdapterName(ADAPTER_NAME);
        nrdpAdapter.getParameters().put("serverURL", "http://localhost:" + PORT + "/context");
        nrdpAdapter.getParameters().put("token", "ri2yu2tfkfkhewfh");
        nrdpAdapter.getParameters().put("passiveCheckSendPeriod", "1000");
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.start();
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithAdapterStartedEvent(ADAPTER_NAME, (BundleExceptionCallable)() -> {
                GatewayActivator.startResourceAdapter(context, ADAPTER_NAME);
                return null;
        }, Duration.ofMinutes(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        try {
            super.afterCleanupTest(context);
        }
        finally {
            server.stop(0);
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
