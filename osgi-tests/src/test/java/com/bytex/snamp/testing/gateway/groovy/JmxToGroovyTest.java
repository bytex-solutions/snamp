package com.bytex.snamp.testing.gateway.groovy;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.gateway.Gateway;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.core.SharedObjectType.COMMUNICATOR;
import static com.bytex.snamp.core.DistributedServices.getProcessLocalObject;
import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.GROOVY_GATEWAY)
public class JmxToGroovyTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String INSTANCE_NAME = "groovy-gateway";
    private static final String GATEWAY_NAME = "groovy";
    private static final String COMMUNICATION_CHANNEL = "test-communication-channel";

    public JmxToGroovyTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    private static String getGroovyScriptPath(){
        return "file:" + getProjectRootDir() + File.separator + "sample-groovy-scripts/";
    }

    @Test
    public void stringAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = getProcessLocalObject(COMMUNICATION_CHANNEL, COMMUNICATOR).orElseThrow(AssertionError::new);
        final String result = channel.sendRequest("changeStringAttribute", Communicator::getPayloadAsString, Duration.ofSeconds(10));
        assertEquals("Frank Underwood", result);
    }

    @Test
    public void booleanAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = getProcessLocalObject(COMMUNICATION_CHANNEL, COMMUNICATOR).orElseThrow(AssertionError::new);
        final Serializable result = channel.sendRequest("changeBooleanAttribute", Communicator.IncomingMessage::getPayload, Duration.ofSeconds(10));
        assertTrue(result instanceof Boolean);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void integerAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = getProcessLocalObject(COMMUNICATION_CHANNEL, COMMUNICATOR).orElseThrow(AssertionError::new);
        final Serializable result = channel.sendRequest("changeIntegerAttribute", Communicator.IncomingMessage::getPayload, Duration.ofSeconds(10));
        assertTrue(result instanceof Integer);
        assertEquals(1020, result);
    }

    @Test
    public void bigIntegerAttributeTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = getProcessLocalObject(COMMUNICATION_CHANNEL, COMMUNICATOR).orElseThrow(AssertionError::new);
        final Serializable result = channel.sendRequest("changeBigIntegerAttribute", Communicator.IncomingMessage::getPayload, Duration.ofSeconds(2));
        assertTrue(result instanceof BigInteger);
        assertEquals(BigInteger.valueOf(1020L), result);
    }

    @Test
    public void notificationTest() throws ExecutionException, TimeoutException, InterruptedException {
        final Communicator channel = getProcessLocalObject(COMMUNICATION_CHANNEL, COMMUNICATOR).orElseThrow(AssertionError::new);
        final String MESSAGE = "changeStringAttributeSilent";
        final Future<Serializable> awaitor = channel.receiveMessage(Communicator.MessageType.RESPONSE, Communicator.IncomingMessage::getPayload);
        channel.sendMessage(MESSAGE, Communicator.MessageType.REQUEST);
        final Serializable notification = awaitor.get(3, TimeUnit.SECONDS);
        assertTrue(notification instanceof Notification);
    }

    @Test
    public void configurationTest(){
        final ConfigurationEntityDescription<?> descr =
                GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, GatewayConfiguration.class);
        testConfigurationDescriptor(descr, "scriptFile",  "scriptPath");
    }

    @Test
    public void attributesBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        try (final GatewayClient client = GatewayClient.tryCreate(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2)).orElseThrow(AssertionError::new)) {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo.getProperty(Gateway.FeatureBindingInfo.MAPPED_TYPE) instanceof WellKnownType));
        }
    }

    @Test
    public void notificationsBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        try (final GatewayClient client = GatewayClient.tryCreate(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2))
                .orElseThrow(AssertionError::new)) {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo != null));
        }
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        final GatewayConfiguration groovyGateway = gateways.getOrAdd(INSTANCE_NAME);
        groovyGateway.setType(GATEWAY_NAME);
        groovyGateway.put("scriptPath", getGroovyScriptPath());
        groovyGateway.put("scriptFile", "Gateway.groovy");
        groovyGateway.put("communicationChannel", COMMUNICATION_CHANNEL);
        groovyGateway.put("resourceName", TEST_RESOURCE_NAME);
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("string");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("boolean");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("int32");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("bigint");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("array");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("dictionary");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("typeName", "dict");

        attribute = attributes.getOrAdd("table");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("typeName", "table");

        attribute = attributes.getOrAdd("float");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("date");
        attribute.put("objectName", BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.put("severity", "notice");
        event.put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.testnotif");
        event.put("severity", "panic");
        event.put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.plainnotif");
        event.put("severity", "notice");
        event.put("objectName", BEAN_NAME);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(GATEWAY_NAME, () -> {
                GatewayActivator.enableGateway(getTestBundleContext(), GATEWAY_NAME);
                return null;
        }, Duration.ofSeconds(15));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_NAME);
        stopResourceConnector(context);
    }
}
