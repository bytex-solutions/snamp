package com.itworks.snamp.testing.adapters.groovy;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.io.Communicator;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;
import java.io.File;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
@SnampDependencies(SnampFeature.GROOVY_ADAPTER)
public class JmxToGroovyTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String INSTANCE_NAME = "groovy-adapter";
    private static final String ADAPTER_NAME = "groovy";
    private static final String COMMUNICATION_CHANNEL = "test-communication-channel";
    private static final Predicate<Object> NON_NOTIF = new Predicate<Object>() {
        @Override
        public boolean apply(final Object responseMessage) {
            return !(responseMessage instanceof Notification);
        }
    };

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
        final Object result = channel.post("changeStringAttribute", NON_NOTIF, TimeSpan.fromSeconds(10));
        assertTrue(result instanceof String);
        assertEquals("Frank Underwood", result);
    }

    @Test
    public void booleanAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = Communicator.getSession(COMMUNICATION_CHANNEL);
        final Object result = channel.post("changeBooleanAttribute", NON_NOTIF, TimeSpan.fromSeconds(10));
        assertTrue(result instanceof Boolean);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void integerAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = Communicator.getSession(COMMUNICATION_CHANNEL);
        final Object result = channel.post("changeIntegerAttribute", NON_NOTIF, TimeSpan.fromSeconds(10));
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
        final SynchronizationEvent<?> awaitor = channel.registerMessageSynchronizer(MESSAGE);
        channel.post(MESSAGE);
        final Object notification = awaitor.getAwaitor().await(TimeSpan.fromSeconds(3));
        assertTrue(notification instanceof Notification);
    }

    @Test
    public void configurationTest(){
        final ResourceAdapterClient client = new ResourceAdapterClient(ADAPTER_NAME);
        final ConfigurationEntityDescription<?> descr =
                client.getConfigurationEntityDescriptor(getTestBundleContext(), ResourceAdapterConfiguration.class);
        assertNotNull(descr);
        final ConfigurationEntityDescription.ParameterDescription parameter = descr.getParameterDescriptor("scriptFile");
        assertNotNull(parameter);
        assertTrue(parameter.isRequired());
        assertFalse(parameter.getDescription(Locale.getDefault()).isEmpty());
    }

    @Test
    public void attributesBindingTest(){
        final Collection<? extends AttributeBindingInfo> attributes = ResourceAdapterClient.getBindingInfo(getTestBundleContext(),
                ADAPTER_NAME,
                INSTANCE_NAME,
                AttributeBindingInfo.class);
        assertFalse(attributes.isEmpty());
    }

    @Test
    public void notificationsBindingTest(){
        final Collection<? extends NotificationBindingInfo> attributes = ResourceAdapterClient.getBindingInfo(getTestBundleContext(),
                ADAPTER_NAME,
                INSTANCE_NAME,
                NotificationBindingInfo.class);
        assertFalse(attributes.isEmpty());
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters,
                                final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration groovyAdapter = adapterFactory.get();
        groovyAdapter.setAdapterName(ADAPTER_NAME);
        groovyAdapter.getParameters().put("scriptPath", getGroovyScriptPath());
        groovyAdapter.getParameters().put("scriptFile", "Adapter.groovy");
        groovyAdapter.getParameters().put("communicationChannel", COMMUNICATION_CHANNEL);
        groovyAdapter.getParameters().put("resourceName", TEST_RESOURCE_NAME);
        adapters.put(INSTANCE_NAME, groovyAdapter);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("1.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("3.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("bi", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "dict");
        attributes.put("6.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "table");
        attributes.put("7.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("8.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("9.0", attribute);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("19.1", event);

        event = eventFactory.get();
        event.setCategory("com.itworks.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("20.1", event);

        event = eventFactory.get();
        event.setCategory("com.itworks.snamp.connectors.tests.impl.plainnotif");
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("21.1", event);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
                return null;
            }
        }, TimeSpan.fromSeconds(15));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }
}
