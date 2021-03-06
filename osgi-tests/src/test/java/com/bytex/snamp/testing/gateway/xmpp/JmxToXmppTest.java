package com.bytex.snamp.testing.gateway.xmpp;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.gateway.xmpp.client.XMPPClient;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableList;
import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.storage.inmemory.MemoryStorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.authorization.Plain;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.MUCModule;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.jivesoftware.smack.packet.Message;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.XMPP_GATEWAY, SnampFeature.WRAPPED_LIBS})
public final class JmxToXmppTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String GATEWAY_NAME = "xmpp";
    private static final String INSTANCE_NAME = "test-xmpp";
    private static final int PORT = 9898;
    private static final String USER_NAME = "agent";
    private static final String PASSWORD = "123";
    private XMPPServer server;

    public JmxToXmppTest() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        gateways.addAndConsume(INSTANCE_NAME, xmppGateway -> {
            xmppGateway.setType(GATEWAY_NAME);
            fillParameters(xmppGateway);
        });
    }

    private static void fillParameters(final Map<String, String> serverParameters){
        serverParameters.put("port", Integer.toString(PORT));
        serverParameters.put("host", "127.0.0.1");
        serverParameters.put("userName", USER_NAME);
        serverParameters.put("password", PASSWORD);
        serverParameters.put("keystore", getPathToFileInProjectRoot("xmpp_tls.cert"));
        serverParameters.put("domain", "bytex.solutions");
        serverParameters.put("keystorePassword", "boguspw");
        serverParameters.put("allowUnsafeCertificate", "true");
    }

    private void testAttribute(final String attributeID,
                               final String value,
                               final Equator<String> equator) throws Exception{
        final Map<String, String> parameters = new HashMap<>(10);
        fillParameters(parameters);
        parameters.put("userName", "tester");
        parameters.put("password", "456");
        try(final XMPPClient client = new XMPPClient(parameters)){
            client.connectAndLogin();
            final String SET = String.format("set -n %s -r %s -v %s --silent", attributeID, TEST_RESOURCE_NAME, value);
            final String GET = String.format("get -n %s -r %s --json", attributeID, TEST_RESOURCE_NAME);
            client.beginChat("agent");
            client.peekMessage(SET);
            Thread.sleep(100);
            final String response = client.sendMessage(GET, "Hi.*", Duration.ofSeconds(10));
            assertTrue(String.format("Expected %s. Actual %s", value, response),
                    equator.equate(response, value));
        }
    }

    private static Equator<String> quotedEquator(){
        return (value1, value2) -> {
            value2 = value2.replace('\'', '\"');
            value1 = '\"' + value1 + '\"';
            return Objects.equals(value1, value2);
        };
    }

    @Test
    public void integerTest() throws Exception {
        testAttribute("3.0", "56", Objects::equals);
    }

    @Test
    public void stringTest() throws Exception {
        testAttribute("1.0", "\"'Hello, world'\"", quotedEquator());
    }

    @Test
    public void notificationTest() throws Exception{
        final String ATTRIBUTE_ID = "3.0";
        final Map<String, String> parameters = new HashMap<>(10);
        fillParameters(parameters);
        parameters.put("userName", "tester");
        parameters.put("password", "456");
        try(final XMPPClient client = new XMPPClient(parameters)) {
            client.connectAndLogin();
            final String SET = String.format("set -n %s -r %s -v %s --silent", ATTRIBUTE_ID, TEST_RESOURCE_NAME, "82");
            client.beginChat("agent");
            //wait for message
            final Future<Message> notifAwaitor =
                    client.waitMessage("Hi.*");
            //enable notifs
            client.peekMessage("notifs -f (severity=notice)");
            //set attribute
            client.peekMessage(SET);
            //Note: do not change Object to Message because Message class is not visible
            //from the PAX test bundle
            final Object notification = notifAwaitor.get(5, TimeUnit.SECONDS);
            assertNotNull(notification);
        }
    }

    @Test
    public void configurationDescriptorTest(){
        final ConfigurationEntityDescription<?> descr = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, GatewayConfiguration.class);
        testConfigurationDescriptor(descr,
                "host",
                "port",
                "userName",
                "domain",
                "password",
                "keystorePassword",
                "keystore",
                "keystoreType",
                "allowUnsafeCertificate",
                "enableM2M");
    }

    @Test
    public void attributesBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        try (final GatewayClient client = GatewayClient.tryCreate(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2))
                .orElseThrow(AssertionError::new)) {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo.getProperty("read-command") instanceof String));
        }
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        server = new XMPPServer("bytex.solutions");
        final TCPEndpoint endpoint = new TCPEndpoint();
        final StorageProviderRegistry providerRegistry = new MemoryStorageProviderRegistry();
        endpoint.setPort(PORT);
        server.addEndpoint(endpoint);
        server.setStorageProviderRegistry(providerRegistry);
        server.setSASLMechanisms(ImmutableList.of(new Plain()));
        final AccountManagement accountManagement =
                (AccountManagement) providerRegistry.retrieve(AccountManagement.class);
        accountManagement.addUser(EntityImpl.parse(USER_NAME + "@bytex.solutions"), PASSWORD);
        accountManagement.addUser(EntityImpl.parse("tester@bytex.solutions"), "456");
        server.setTLSCertificateInfo(new File(getPathToFileInProjectRoot("xmpp_tls.cert")), "boguspw");
        server.start();
        server.addModule(new MUCModule());
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
            server.stop();
            server = null;
        }
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("enableM2M", "true");

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");
        attribute.put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.put("objectName", BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        event.setAlternativeName(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.put("severity", "notice");
        event.put("objectName", BEAN_NAME);
        event.put("enableM2M", "true");
    }
}
