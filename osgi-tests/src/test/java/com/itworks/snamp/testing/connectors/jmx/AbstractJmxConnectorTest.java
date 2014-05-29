package com.itworks.snamp.testing.connectors.jmx;

import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.AbstractManagementConnectorTest;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * Represents a base class for JMX management connector tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractJmxConnectorTest<MBean> extends AbstractManagementConnectorTest {
    private final ObjectName beanName;
    protected final MBean beanInstance;
    protected static final String CONNECTOR_NAME = "jmx";

    protected AbstractJmxConnectorTest(final MBean beanInstance, final ObjectName beanName){
        super(CONNECTOR_NAME, getJmxConnectionString(), SnampArtifact.JMX_CONNECTOR.getReference());
        this.beanName = beanName;
        this.beanInstance = beanInstance;
    }

    private static String getJmxConnectionString(){
        final String jmxPort =
                System.getProperty("com.sun.management.jmxremote.port", "9010");
        return String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi", jmxPort);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if(mbs.isRegistered(beanName))
            mbs.unregisterMBean(beanName);
        mbs.registerMBean(beanInstance, beanName);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.unregisterMBean(beanName);
    }
}