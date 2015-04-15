package com.itworks.snamp.testing.adapters.xmpp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.xmpp.client.XMPPClient;
import com.itworks.snamp.configuration.AbsentConfigurationParameterException;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.storage.inmemory.MemoryStorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.authorization.Plain;
import org.apache.vysper.xmpp.authorization.SASLMechanism;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.MUCModule;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.XMPP_ADAPTER, SnampFeature.WRAPPED_LIBS})
public final class XmppAdapterTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String ADAPTER_NAME = "xmpp";
    private static final String ADAPTER_INSTANCE = "test-xmpp";
    private static final int PORT = 9898;
    private static final String USER_NAME = "agent";
    private static final String PASSWORD = "123";
    private XMPPServer server;

    public XmppAdapterTest() throws MalformedObjectNameException, IOException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters,
                                final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration xmppAdapter = adapterFactory.get();
        xmppAdapter.setAdapterName(ADAPTER_NAME);
        fillParameters(xmppAdapter.getParameters());
        adapters.put(ADAPTER_INSTANCE, xmppAdapter);
    }

    private static void fillParameters(final Map<String, String> serverParameters){
        serverParameters.put("port", Integer.toString(PORT));
        serverParameters.put("host", "127.0.0.1");
        serverParameters.put("userName", USER_NAME);
        serverParameters.put("password", PASSWORD);
        serverParameters.put("keystore", getPathToFileInProjectRoot("xmpp_tls.cert"));
        serverParameters.put("domain", "itworks.com");
        serverParameters.put("keystorePassword", "boguspw");
        serverParameters.put("allowUnsafeCertificate", "true");
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    private void testAttribute(final String attributeID) throws AbsentConfigurationParameterException, GeneralSecurityException, IOException, TimeoutException, InterruptedException{
        final Map<String, String> parameters = new HashMap<>(10);
        fillParameters(parameters);
        parameters.put("userName", "tester");
        parameters.put("password", "456");
        try(final XMPPClient client = new XMPPClient(parameters)){
            client.connectAndLogin();
            final String command = String.format("get -n %s -r %s --json", attributeID, TEST_RESOURCE_NAME);
            client.beginChat("agent");
            final String response = client.sendMessageSync(command, "Hi.*", TimeSpan.fromSeconds(60));
            response.toString();
        }
    }

    @Test
    public void simpleTest() throws AbsentConfigurationParameterException, GeneralSecurityException, IOException, TimeoutException, InterruptedException {
        testAttribute("3.0");
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        server = new XMPPServer("itworks.com");
        final TCPEndpoint endpoint = new TCPEndpoint();
        final StorageProviderRegistry providerRegistry = new MemoryStorageProviderRegistry();
        endpoint.setPort(PORT);
        server.addEndpoint(endpoint);
        server.setStorageProviderRegistry(providerRegistry);
        server.setSASLMechanisms(ImmutableList.<SASLMechanism>of(new Plain()));
        final AccountManagement accountManagement =
                (AccountManagement) providerRegistry.retrieve(AccountManagement.class);
        accountManagement.addUser(EntityImpl.parse(USER_NAME + "@itworks.com"), PASSWORD);
        accountManagement.addUser(EntityImpl.parse("tester@itworks.com"), "456");
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
            server.stop();
            server = null;
        }
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("3.0", attribute);
    }
}
