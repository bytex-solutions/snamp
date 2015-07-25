package com.itworks.snamp.testing.adapters.nrdp;

import com.google.common.base.Supplier;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapter;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.concurrent.Awaitor;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.io.IOUtils;
import com.itworks.snamp.jmx.DescriptorUtils;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
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

    private static final class Mailbox extends SynchronizationEvent<Void> implements HttpHandler{
        @Override
        public void handle(final HttpExchange exchange) throws IOException {
            final byte[] content = IOUtils.readFully(exchange.getRequestBody());
            assertTrue(content != null);
            assertTrue(content.length > 0);
            fire(null);
            exchange.close();
        }
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void testIntAttribute() throws JMException, IOException, InterruptedException, TimeoutException {
        final ManagedResourceConnector connector = getManagementConnector();
        try{
            connector.setAttribute(new Attribute("3.0", 80));
            final Mailbox listener = new Mailbox();
            final Awaitor<?, ExceptionPlaceholder> awaitor = listener.getAwaitor();
            final HttpContext context = server.createContext("/context", listener);
            try{
                awaitor.await(TimeSpan.fromSeconds(2));
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
        ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        testConfigurationDescriptor(desc, "serverURL", "connectionTimeout", "token", "passiveCheckSendPeriod");
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, AttributeConfiguration.class);
        testConfigurationDescriptor(desc, "serviceName", "maxValue", "minValue", "units");
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, EventConfiguration.class);
        testConfigurationDescriptor(desc, "serviceName");
    }

    @Test
    public void attributeBindingTest() throws TimeoutException, InterruptedException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.fromSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, new RecordReader<String, ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo>, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String resourceName, final ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo> bindingInfo) {
                    return bindingInfo != null;
                }
            }));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters,
                                final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration nrdpAdapter = adapterFactory.get();
        nrdpAdapter.setAdapterName(ADAPTER_NAME);
        nrdpAdapter.getParameters().put("serverURL", "http://localhost:" + PORT + "/context");
        nrdpAdapter.getParameters().put("token", "ri2yu2tfkfkhewfh");
        nrdpAdapter.getParameters().put("passiveCheckSendPeriod", "1000");
        adapters.put(INSTANCE_NAME, nrdpAdapter);
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
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(context, ADAPTER_NAME);
                return null;
            }
        }, TimeSpan.fromMinutes(4));
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
            server.stop(0);
            server = null;
        }
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("serviceName", "memory");
        attribute.getParameters().put(DescriptorUtils.MAX_VALUE_FIELD, "100");
        attribute.getParameters().put(DescriptorUtils.MIN_VALUE_FIELD, "0");
        attribute.getParameters().put(DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD, "MB");
        attributes.put("3.0", attribute);
    }
}
