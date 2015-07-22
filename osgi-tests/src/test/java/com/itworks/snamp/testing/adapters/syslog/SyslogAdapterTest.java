package com.itworks.snamp.testing.adapters.syslog;

import com.google.common.base.Supplier;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapter;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.io.IOUtils;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.SYSLOG_ADAPTER)
public final class SyslogAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "syslog";
    private static final String INSTANCE_NAME = "test-syslog";
    private ServerSocket server;
    private static final int PORT = 9652;

    public SyslogAdapterTest() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void testIntAttribute() throws JMException, IOException, InterruptedException {
        final ManagedResourceConnector connector = getManagementConnector();
        try{
            connector.setAttribute(new Attribute("3.0", 80));
            try(final Socket socket = server.accept()){
                assertTrue(IOUtils.waitForData(socket.getInputStream(), 1000L));
            }
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, EventConfiguration.class);
        assertNotNull(desc);
        ConfigurationEntityDescription.ParameterDescription param = desc.getParameterDescriptor("severity");
        assertNotNull(param);
        assertFalse(param.getDescription(null).isEmpty());
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        assertNotNull(desc);
        param = desc.getParameterDescriptor("port");
        assertNotNull(param);
        assertFalse(param.getDescription(null).isEmpty());
    }

    @Test
    public void attributesBindingTest() throws TimeoutException, InterruptedException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.fromSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, new RecordReader<String, ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo>, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String resourceName, final ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo> bindingInfo) {
                    return bindingInfo.getProperty("messageID") instanceof String &&
                            bindingInfo.getProperty("facility") instanceof Enum<?>;
                }
            }));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters,
                                final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration syslogAdapter = adapterFactory.get();
        syslogAdapter.setAdapterName(ADAPTER_NAME);
        syslogAdapter.getParameters().put("port", Integer.toString(PORT));
        syslogAdapter.getParameters().put("address", "127.0.0.1");
        syslogAdapter.getParameters().put("protocol", "tcp");
        adapters.put(INSTANCE_NAME, syslogAdapter);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("3.0", attribute);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("19.1", event);
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
            server.close();
            server = null;
        }
    }
}
