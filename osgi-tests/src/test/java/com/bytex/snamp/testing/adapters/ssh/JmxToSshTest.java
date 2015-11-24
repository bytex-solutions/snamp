package com.bytex.snamp.testing.adapters.ssh;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapter;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.internal.EntryReader;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * Represents SSH-to-RShell integration test.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.SSH_ADAPTER)
public final class JmxToSshTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String FINGERPRINT = "e8:0d:af:84:bb:ec:05:03:b9:7c:f3:75:19:5a:2a:63";
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;
    private static final String ADAPTER_NAME = "ssh";
    private static final String INSTANCE_NAME = "test-ssh";

    public JmxToSshTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(/*true*/), new ObjectName(BEAN_NAME));
    }

    private static void testScalarAttribute(final String attributeName,
                               final String value,
                               final Equator<String> equator) throws IOException{
        try(final SSHClient client = new SSHClient()){
            client.addHostKeyVerifier(FINGERPRINT);
            client.connect("localhost", PORT);
            client.authPassword(USER_NAME, PASSWORD);
            try(final Session s = client.startSession()) {
                s.exec(String.format("set -n %s -r %s -v %s", attributeName, TEST_RESOURCE_NAME, value));
            }
            try(final Session s = client.startSession()){
                final Session.Command result = s.exec(String.format("get -n %s -r %s --json", attributeName, TEST_RESOURCE_NAME));
                final String output = IOUtils.readFully(result.getInputStream()).toString();
                final String error = IOUtils.readFully(result.getErrorStream()).toString();
                if(error != null && error.length() > 0)
                    fail(error);
                assertNotNull(output);
                assertFalse(output.isEmpty());
                assertTrue(equator.equate(output, value));
            }
        }
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void integerTest() throws IOException {
        testScalarAttribute("3.0", "42", AbstractResourceConnectorTest.<String>valueEquator());
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
    public void stringTest() throws IOException{
        testScalarAttribute("1.0", "\"'Hello, world'\"", quotedEquator());
    }

    @Test
    public void booleanTest() throws IOException{
        testScalarAttribute("2.0", "true", AbstractResourceConnectorTest.<String>valueEquator());
    }

    @Test
    public void bigIntTest() throws IOException{
        testScalarAttribute("4.0", "10005000", AbstractResourceConnectorTest.<String>valueEquator());
    }

    @Test
    public void floatTest() throws IOException{
        testScalarAttribute("8.0", "3.141", AbstractResourceConnectorTest.<String>valueEquator());
    }

    @Test
    public void arrayTest() throws IOException{
        testScalarAttribute("5.1", "[42,100,332,99]", AbstractResourceConnectorTest.<String>valueEquator());
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
        }, TimeSpan.ofSeconds(60));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        final ResourceAdapterConfiguration sshAdapter = adapters.getOrAdd(INSTANCE_NAME);
        sshAdapter.setAdapterName(ADAPTER_NAME);
        sshAdapter.getParameters().put("host", "0.0.0.0");
        sshAdapter.getParameters().put("port", Integer.toString(PORT));
        sshAdapter.getParameters().put("userName", USER_NAME);
        sshAdapter.getParameters().put("password", PASSWORD);
        sshAdapter.getParameters().put("hostKeyFile", getPathToFileInProjectRoot("hostkey.ser"));
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("2.0");
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("4.0");
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("5.1");
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("20.1");
        event.setCategory("com.bytex.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
    }
}
