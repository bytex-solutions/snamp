package com.bytex.snamp.testing.gateway.smtp;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.reflect.TypeToken;
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.time.Duration;

import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;

/**
 * Provides integration test for SMTP gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies({SnampFeature.WRAPPED_LIBS, SnampFeature.SMTP_GATEWAY})
public final class JmxToSmtpGatewayTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private static final String GATEWAY_NAME = "smtp";

    private GreenMail smtpServer;

    public JmxToSmtpGatewayTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        //https://stackoverflow.com/questions/6552829/using-javamail-and-greenmail-for-smtps-ssl
        //Security.setProperty(SOCKET_FACTORY_PROVIDER_PARAM, DummySSLSocketFactory.class.getName());
        smtpServer = new GreenMail(ServerSetupTest.SMTPS);
        smtpServer.setUser("receiver@bytex.solutions", "receiver", "123");
        smtpServer.setUser("sender@bytex.solutions", "sender", "456");
        smtpServer.start();
        smtpServer.getSmtps().checkAccess();
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Test
    public void attributeChangeTest() throws JMException, InterruptedException {
        testAttribute("int32", TypeToken.of(Integer.class), 42);
        assertTrue(smtpServer.waitForIncomingEmail(4_000_000, 1));
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(GATEWAY_NAME, () -> {
            GatewayActivator.enableGateway(context, GATEWAY_NAME);
            return null;
        }, Duration.ofSeconds(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        //Security.setProperty(SOCKET_FACTORY_PROVIDER_PARAM, socketFactoryProvider);
        smtpServer.stop();
        smtpServer = null;
        super.afterCleanupTest(context);
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        gateways.addAndConsume("gw", gateway -> {
            gateway.setType(GATEWAY_NAME);
            gateway.put("userName", "sender");
            gateway.put("password", "456");
            gateway.put("from", "sender@bytex.solutions");
            gateway.put("to", "receiver@bytex.solutions");
            gateway.put("port", Integer.toString(ServerSetupTest.SMTPS.getPort()));
            gateway.put("host", "127.0.0.1");
            gateway.put("enableTLS", "false");  
            //TLS settings: https://www.mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/
            gateway.put("mail.smtp.socketFactory.port", Integer.toString(ServerSetupTest.SMTPS.getPort()));
            gateway.put("mail.smtp.socketFactory.class", DummySSLSocketFactory.class.getName());
        });
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
}
