package com.bytex.snamp.testing.adapters.syslog;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapter;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.internal.EntryReader;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

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
                assertTrue(IOUtils.waitForData(socket.getInputStream(), TimeSpan.ofSeconds(1L)));
            }
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        ConfigurationEntityDescription desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, EventConfiguration.class);
        testConfigurationDescriptor(desc,
                "applicationName",
                "facility",
                "severity");
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, AttributeConfiguration.class);
        testConfigurationDescriptor(desc,
                "applicationName",
                "facility");
        desc = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        testConfigurationDescriptor(desc,
                "port",
                "address",
                "ssl",
                "messageFormat",
                "protocol",
                "passiveCheckSendPeriod",
                "connectionTimeout");
    }

    @Test
    public void attributesBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, new EntryReader<String, ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo>, ExceptionPlaceholder>() {
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
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        final ResourceAdapterConfiguration syslogAdapter = adapters.getOrAdd(INSTANCE_NAME);
        syslogAdapter.setAdapterName(ADAPTER_NAME);
        syslogAdapter.getParameters().put("port", Integer.toString(PORT));
        syslogAdapter.getParameters().put("address", "127.0.0.1");
        syslogAdapter.getParameters().put("protocol", "tcp");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("3.0");
        setFeatureName(attribute, "int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        setFeatureName(event, AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
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
        }, TimeSpan.ofMinutes(4));
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
