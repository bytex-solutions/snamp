package com.bytex.snamp.testing.adapters.xmpp;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapter;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.adapters.xmpp.client.XMPPClient;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.internal.EntryReader;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableList;
import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.storage.inmemory.MemoryStorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.authorization.Plain;
import org.apache.vysper.xmpp.authorization.SASLMechanism;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.MUCModule;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.jivesoftware.smack.packet.Message;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
@SnampDependencies({SnampFeature.XMPP_ADAPTER, SnampFeature.WRAPPED_LIBS})
public final class JmxToXmppTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "xmpp";
    private static final String INSTANCE_NAME = "test-xmpp";
    private static final int PORT = 9898;
    private static final String USER_NAME = "agent";
    private static final String PASSWORD = "123";
    private XMPPServer server;

    public JmxToXmppTest() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    @Override
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        final ResourceAdapterConfiguration xmppAdapter = adapters.getOrAdd(INSTANCE_NAME);
        xmppAdapter.setAdapterName(ADAPTER_NAME);
        fillParameters(xmppAdapter.getParameters());
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

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
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
            final String response = client.sendMessage(GET, "Hi.*", TimeSpan.ofSeconds(10));
            assertTrue(String.format("Expected %s. Actual %s", value, response),
                    equator.equate(response, value));
        }
    }

    private static Equator<String> quotedEquator(){
        return new Equator<String>() {
            @Override
            public boolean equate(String value1, String value2) {
                value2 = value2.replace('\'', '\"');
                value1 = '\"' + value1 + '\"';
                return Objects.equals(value1, value2);
            }
        };
    }

    @Test
    public void integerTest() throws Exception {
        testAttribute("3.0", "56", AbstractResourceConnectorTest.<String>valueEquator());
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
        final ConfigurationEntityDescription<?> descr = ResourceAdapterClient.getConfigurationEntityDescriptor(getTestBundleContext(), ADAPTER_NAME, ResourceAdapterConfiguration.class);
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
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, new EntryReader<String, ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo>, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String resourceName, final ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo> bindingInfo) {
                    return bindingInfo.getProperty("read-command") instanceof String;
                }
            }));
        } finally {
            client.release(getTestBundleContext());
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
        server.setSASLMechanisms(ImmutableList.<SASLMechanism>of(new Plain()));
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
            server.stop();
            server = null;
        }
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        setFeatureName(attribute, "string");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("2.0");
        setFeatureName(attribute, "boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        setFeatureName(attribute, "int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("enableM2M", "true");

        attribute = attributes.getOrAdd("4.0");
        setFeatureName(attribute, "bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("5.1");
        setFeatureName(attribute, "array");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        setFeatureName(attribute, "float");
        attribute.getParameters().put("objectName", BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        setFeatureName(event, AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("enableM2M", "true");
    }
}
