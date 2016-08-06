package com.bytex.snamp.testing.adapters.groovy;

import com.bytex.snamp.adapters.ResourceAdapter;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.io.Communicator;
import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.io.File;
import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

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
@SnampDependencies(SnampFeature.GROOVY_ADAPTER)
public class JmxToGroovyTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String INSTANCE_NAME = "groovy-adapter";
    private static final String ADAPTER_NAME = "groovy";
    private static final String COMMUNICATION_CHANNEL = "test-communication-channel";
    private static final Predicate<Object> NON_NOTIF = responseMessage -> !(responseMessage instanceof Notification);

    public JmxToGroovyTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    private static String getGroovyScriptPath(){
        return getProjectRootDir() + File.separator + "sample-groovy-scripts/";
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void stringAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = Communicator.getSession(COMMUNICATION_CHANNEL);
        final Object result = channel.post("changeStringAttribute", NON_NOTIF, Duration.ofSeconds(10));
        assertTrue(result instanceof String);
        assertEquals("Frank Underwood", result);
    }

    @Test
    public void booleanAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = Communicator.getSession(COMMUNICATION_CHANNEL);
        final Object result = channel.post("changeBooleanAttribute", NON_NOTIF, Duration.ofSeconds(10));
        assertTrue(result instanceof Boolean);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void integerAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = Communicator.getSession(COMMUNICATION_CHANNEL);
        final Object result = channel.post("changeIntegerAttribute", NON_NOTIF, Duration.ofSeconds(10));
        assertTrue(result instanceof Integer);
        assertEquals(1020, result);
    }

    @Test
    public void bigIntegerAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = Communicator.getSession(COMMUNICATION_CHANNEL);
        final Object result = channel.post("changeBigIntegerAttribute", NON_NOTIF, 2000);
        assertTrue(result instanceof BigInteger);
        assertEquals(BigInteger.valueOf(1020L), result);
    }

    @Test
    public void notificationTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = Communicator.getSession(COMMUNICATION_CHANNEL);
        final String MESSAGE = "changeStringAttributeSilent";
        final Future<?> awaitor = channel.registerMessageSynchronizer(MESSAGE);
        channel.post(MESSAGE);
        final Object notification = awaitor.get(3, TimeUnit.SECONDS);
        assertTrue(notification instanceof Notification);
    }

    @Test
    public void configurationTest(){
        final ConfigurationEntityDescription<?> descr =
                ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
        testConfigurationDescriptor(descr, "scriptFile",  "scriptPath");
    }

    @Test
    public void attributesBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo.getProperty(ResourceAdapter.FeatureBindingInfo.MAPPED_TYPE) instanceof WellKnownType));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Test
    public void notificationsBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo != null));
        } finally {
            client.release(getTestBundleContext());
        }
    }

    @Override
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        final ResourceAdapterConfiguration groovyAdapter = adapters.getOrAdd(INSTANCE_NAME);
        groovyAdapter.setAdapterName(ADAPTER_NAME);
        groovyAdapter.getParameters().put("scriptPath", getGroovyScriptPath());
        groovyAdapter.getParameters().put("scriptFile", "Adapter.groovy");
        groovyAdapter.getParameters().put("communicationChannel", COMMUNICATION_CHANNEL);
        groovyAdapter.getParameters().put("resourceName", TEST_RESOURCE_NAME);
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("string");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("array");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "dict");

        attribute = attributes.getOrAdd("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "table");

        attribute = attributes.getOrAdd("float");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connectors.tests.impl.plainnotif");
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithAdapterStartedEvent(ADAPTER_NAME, (BundleExceptionCallable)() -> {
                ResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
                return null;
        }, Duration.ofSeconds(15));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }
}
