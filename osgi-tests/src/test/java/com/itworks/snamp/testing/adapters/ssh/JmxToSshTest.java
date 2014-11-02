package com.itworks.snamp.testing.adapters.ssh;

import com.google.common.base.Supplier;
import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Map;

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

    @Test
    public void getIntegerTest() throws IOException {
        try(final SSHClient client = new SSHClient()){
            client.addHostKeyVerifier(FINGERPRINT);
            client.connect("localhost", PORT);
            client.authPassword(USER_NAME, PASSWORD);
            final String INT_ATTR = String.format("%s/%s", TEST_RESOURCE_NAME, "3.0");
            try(final Session s = client.startSession()) {
                s.exec(String.format("set " + INT_ATTR + " 42"));
            }
            try(final Session s = client.startSession()){
                final String value = IOUtils.readFully(s.exec("get " + INT_ATTR).getInputStream()).toString();
                assertNotNull(value);
                assertFalse(value.isEmpty());
                assertEquals("42", value);
            }
        }
    }

    /*@Test
    public void simpleHostTest() throws InterruptedException {
        Thread.sleep(10000000);
    }*/

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        super.afterStartTest(context);
        AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        AbstractResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
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
