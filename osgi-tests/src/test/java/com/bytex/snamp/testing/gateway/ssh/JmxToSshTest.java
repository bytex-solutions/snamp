package com.bytex.snamp.testing.gateway.ssh;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;

/**
 * Represents SSH-to-RShell integration test.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.SSH_GATEWAY)
public final class JmxToSshTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String FINGERPRINT = "e8:0d:af:84:bb:ec:05:03:b9:7c:f3:75:19:5a:2a:63";
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;
    private static final String GATEWAY_NAME = "ssh";
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
        testScalarAttribute("3.0", "42", Objects::equals);
    }

    private static Equator<String> quotedEquator(){
        return (value1, value2) -> {
            value2 = value2.replace('\'', '\"');
            value1 = '\"' + value1 + '\"';
            return Objects.equals(value1, value2);
        };
    }

    @Test
    public void stringTest() throws IOException{
        testScalarAttribute("1.0", "\"'Hello, world'\"", quotedEquator());
    }

    @Test
    public void booleanTest() throws IOException{
        testScalarAttribute("2.0", "true", Objects::equals);
    }

    @Test
    public void bigIntTest() throws IOException{
        testScalarAttribute("4.0", "10005000", Objects::equals);
    }

    @Test
    public void floatTest() throws IOException{
        testScalarAttribute("8.0", "3.141", Objects::equals);
    }

    @Test
    public void arrayTest() throws IOException{
        testScalarAttribute("5.1", "[42,100,332,99]", Objects::equals);
    }

    @Test
    public void attributesBindingTest() throws TimeoutException, InterruptedException, ExecutionException {
        final GatewayClient client = new GatewayClient(getTestBundleContext(), INSTANCE_NAME, Duration.ofSeconds(2));
        try {
            assertTrue(client.forEachFeature(MBeanAttributeInfo.class, (resourceName, bindingInfo) -> bindingInfo.getProperty("read-command") instanceof String));
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
        syncWithGatewayStartedEvent(GATEWAY_NAME, () -> {
                GatewayActivator.enableGateway(context, GATEWAY_NAME);
                return null;
        }, Duration.ofSeconds(60));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        gateways.addAndConsume(INSTANCE_NAME, sshGateway -> {
            sshGateway.setType(GATEWAY_NAME);
            sshGateway.getParameters().put("host", "0.0.0.0");
            sshGateway.getParameters().put("port", Integer.toString(PORT));
            sshGateway.getParameters().put("userName", USER_NAME);
            sshGateway.getParameters().put("password", PASSWORD);
            sshGateway.getParameters().put("hostKeyFile", getPathToFileInProjectRoot("hostkey.ser"));
        });
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        event.setAlternativeName(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("20.1");
        event.setAlternativeName("com.bytex.snamp.connector.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
    }
}
