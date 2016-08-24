package com.bytex.snamp.testing.connector.composite;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.RSHELL_CONNECTOR, SnampFeature.JMX_CONNECTOR})
public final class RShellWithJmxCompositionTest extends AbstractCompositeConnectorTest {
    private static String buildConnectionString() {
        return "jmx:=" +
                AbstractJmxConnectorTest.getConnectionString() +
                ';' +
                "rshell:=process";
    }

    private final ObjectName beanName;
    private final TestOpenMBean beanInstance;


    public RShellWithJmxCompositionTest() throws MalformedObjectNameException {
        super(buildConnectionString(), ImmutableMap.of(
            "jmx:login", AbstractJmxConnectorTest.JMX_LOGIN,
            "jmx:password", AbstractJmxConnectorTest.JMX_PASSWORD,
            "jmx:objectName", TestOpenMBean.BEAN_NAME
        ));
        beanName = new ObjectName(TestOpenMBean.BEAN_NAME);
        beanInstance = new TestOpenMBean();
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    @Test
    public void stringAttributeTest() throws JMException {
        testAttribute("jmx:str", TypeToken.of(String.class), "Frank Underwood");
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws JMException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if(mbs.isRegistered(beanName))
            mbs.unregisterMBean(beanName);
        mbs.registerMBean(beanInstance, beanName);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws JMException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.unregisterMBean(beanName);
    }

    @Override
    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations) {
        operations.addAndConsume("jmx:forceGC", operation -> operation.setAlternativeName("gc"));
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        events.addAndConsume("jmx:attributeChange", event -> {
            event.setAlternativeName(AttributeChangeNotification.ATTRIBUTE_CHANGE);
            event.getParameters().put("severity", "notice");
            event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        });
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("jmx:str", attribute -> {
            attribute.setAlternativeName("string");
            attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        });

        attributes.addAndConsume("jmx:bool", attribute -> {
            attribute.setAlternativeName("boolean");
            attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        });

        attributes.addAndConsume("rshell:ms", attribute -> {
            attribute.setAlternativeName(getPathToFileInProjectRoot("freemem-tool-profile.xml"));
            attribute.getParameters().put("format", "-m");
        });
    }
}
