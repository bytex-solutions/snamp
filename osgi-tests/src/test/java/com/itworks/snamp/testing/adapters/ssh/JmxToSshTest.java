package com.itworks.snamp.testing.adapters.ssh;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestOpenMBean;
import org.apache.commons.collections4.Factory;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
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
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;
    private static final String ADAPTER_NAME = "ssh";

    public JmxToSshTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME),
                mavenBundle("jline", "jline", "2.12"),
                SnampArtifact.SSH_ADAPTER.getReference());
    }

    @Test
    public void simpleHostTest() throws InterruptedException {
        Thread.sleep(10000000);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        super.afterStartTest(context);
        AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        AbstractResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Factory<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration restAdapter = adapterFactory.create();
        restAdapter.setAdapterName(ADAPTER_NAME);
        restAdapter.getHostingParams().put("host", "0.0.0.0");
        restAdapter.getHostingParams().put("port", Integer.toString(PORT));
        restAdapter.getHostingParams().put("userName", USER_NAME);
        restAdapter.getHostingParams().put("password", PASSWORD);
        restAdapter.getHostingParams().put("tty-options", "echo");
        adapters.put("test-jmx", restAdapter);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Factory<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.create();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("1.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("3.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("4.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("6.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("7.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("8.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attributes.put("9.0", attribute);
    }
}
