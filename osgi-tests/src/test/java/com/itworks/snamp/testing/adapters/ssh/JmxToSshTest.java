package com.itworks.snamp.testing.adapters.ssh;

import com.google.common.base.Supplier;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Represents SSH-to-RShell integration test.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxToSshTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String FINGERPRINT = "24:aa:e0:cb:d9:89:1d:68:f3:1d:ed:53:0e:99:31:87";
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;
    private static final String ADAPTER_NAME = "ssh";

    public JmxToSshTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(/*true*/), new ObjectName(BEAN_NAME),
                mavenBundle("jline", "jline", "2.12"),
                SnampArtifact.SSHJ.getReference(),
                SnampArtifact.SSH_ADAPTER.getReference());
    }

    private void testScalarAttribute(String attributeId,
                               final String value,
                               final Equator<String> equator) throws IOException{
        try(final SSHClient client = new SSHClient()){
            client.addHostKeyVerifier(FINGERPRINT);
            client.connect("localhost", PORT);
            client.authPassword(USER_NAME, PASSWORD);
            attributeId = String.format("%s/%s", TEST_RESOURCE_NAME, attributeId);
            try(final Session s = client.startSession()) {
                s.exec(String.format("set %s %s", attributeId, value));
            }
            try(final Session s = client.startSession()){
                final Session.Command result = s.exec(String.format("get %s", attributeId));
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

    @Test
    public void integerTest() throws IOException {
        testScalarAttribute("3.0", "42", AbstractResourceConnectorTest.<String>valueEquator());
    }

    @Test
    public void stringTest() throws IOException{
        testScalarAttribute("1.0", "\"Hello, world\"", new Equator<String>() {
            @Override
            public boolean equate(final String value1, final String value2) {
                return Objects.equals(String.format("\"%s\"", value1), value2);
            }
        });
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
    public void tableTest() throws IOException{
        try(final SSHClient client = new SSHClient()){
            client.addHostKeyVerifier(FINGERPRINT);
            client.connect("localhost", PORT);
            client.authPassword(USER_NAME, PASSWORD);
            final String attributeId = String.format("%s/%s", TEST_RESOURCE_NAME, "7.1");
            //update dictionary
            try(final Session s = client.startSession()) {
                s.exec(String.format("set-table %s -r col1=false -r col2=2 -r col3=pp -i 0", attributeId));
            }
            try(final Session s = client.startSession()){
                final String result = IOUtils.readFully(s.exec(String.format("get %s", attributeId)).getInputStream()).toString();
                assertNotNull(result);
                assertFalse(result.isEmpty());
                assertEquals("TABLE col1\tcol2\tcol3false\tpp\t2false\tCiao, monde!\t42true\tLuke Skywalker\t1", result);
            }
        }
    }

    @Test
    public void dictionaryTest() throws IOException{
        try(final SSHClient client = new SSHClient()){
            client.addHostKeyVerifier(FINGERPRINT);
            client.connect("localhost", PORT);
            client.authPassword(USER_NAME, PASSWORD);
            final String attributeId = String.format("%s/%s", TEST_RESOURCE_NAME, "6.1");
            //update dictionary
            try(final Session s = client.startSession()) {
                s.exec(String.format("set-map %s -p col1=false -p col2=42", attributeId));
            }
            try(final Session s = client.startSession()){
                final String result = IOUtils.readFully(s.exec(String.format("get %s", attributeId)).getInputStream()).toString();
                assertNotNull(result);
                assertFalse(result.isEmpty());
                assertTrue(result.startsWith("MAP "));
                assertTrue(result.contains("col1=false"));
                assertTrue(result.contains("col2=42"));
            }
        }
    }

    @Test
    public void arrayTest() throws IOException{
        try(final SSHClient client = new SSHClient()){
            client.addHostKeyVerifier(FINGERPRINT);
            client.connect("localhost", PORT);
            client.authPassword(USER_NAME, PASSWORD);
            final String attributeId = String.format("%s/%s", TEST_RESOURCE_NAME, "5.1");
            //update array element
            try(final Session s = client.startSession()) {
                s.exec(String.format("set-array %s -i 2 -v 332", attributeId));
            }
            try(final Session s = client.startSession()){
                final String result = IOUtils.readFully(s.exec(String.format("get %s", attributeId)).getInputStream()).toString();
                assertEquals("ARRAY = [42, 100, 332, 99]", result);
            }
            //delete array element
            try(final Session s = client.startSession()) {
                s.exec(String.format("set-array %s -i 2 -d", attributeId));
            }
            try(final Session s = client.startSession()){
                final String result = IOUtils.readFully(s.exec(String.format("get %s", attributeId)).getInputStream()).toString();
                assertEquals("ARRAY = [42, 100, 99]", result);
            }
            //insert array element
            try(final Session s = client.startSession()) {
                s.exec(String.format("set-array %s -i 1 -v 456 -a", attributeId));
            }
            try(final Session s = client.startSession()){
                final String result = IOUtils.readFully(s.exec(String.format("get %s", attributeId)).getInputStream()).toString();
                assertEquals("ARRAY = [42, 456, 100, 99]", result);
            }
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
        final ResourceAdapterConfiguration restAdapter = adapterFactory.get();
        restAdapter.setAdapterName(ADAPTER_NAME);
        restAdapter.getParameters().put("host", "0.0.0.0");
        restAdapter.getParameters().put("port", Integer.toString(PORT));
        restAdapter.getParameters().put("userName", USER_NAME);
        restAdapter.getParameters().put("password", PASSWORD);
        restAdapter.getParameters().put("tty-options", "echo");
        adapters.put("test-jmx", restAdapter);
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
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("6.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("columnBasedPrint", "true");
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
    }
}
