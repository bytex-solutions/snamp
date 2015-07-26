package com.bytex.snamp.testing.adapters.ssh;

import com.google.common.base.Supplier;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.ResourceAdapter;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.internal.RecordReader;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

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

    private void testScalarAttribute(final String attributeName,
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
    public void attributesBindingTest() throws TimeoutException, InterruptedException {
        final ResourceAdapterClient client = new ResourceAdapterClient(getTestBundleContext(), INSTANCE_NAME, TimeSpan.fromSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, new RecordReader<String, ResourceAdapter.FeatureBindingInfo<MBeanAttributeInfo>, ExceptionPlaceholder>() {
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
    protected void afterStartTest(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        startResourceConnector(context);
        syncWithAdapterStartedEvent(ADAPTER_NAME, new ExceptionalCallable<Void, BundleException>() {
            @Override
            public Void call() throws BundleException {
                ResourceAdapterActivator.startResourceAdapter(context, ADAPTER_NAME);
                return null;
            }
        }, TimeSpan.fromSeconds(60));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Supplier<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration sshAdapter = adapterFactory.get();
        sshAdapter.setAdapterName(ADAPTER_NAME);
        sshAdapter.getParameters().put("host", "0.0.0.0");
        sshAdapter.getParameters().put("port", Integer.toString(PORT));
        sshAdapter.getParameters().put("userName", USER_NAME);
        sshAdapter.getParameters().put("password", PASSWORD);
        sshAdapter.getParameters().put("hostKeyFile", getPathToFileInProjectRoot("hostkey.ser"));
        adapters.put(INSTANCE_NAME, sshAdapter);
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
        attributes.put("4.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("8.0", attribute);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("19.1", event);

        event = eventFactory.get();
        event.setCategory("com.bytex.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
        events.put("20.1", event);
    }
}
