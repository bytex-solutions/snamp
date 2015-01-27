package com.itworks.snamp.testing.connectors.jmx;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;

/**
 * Represents a base class for JMX management connector tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.JMX_CONNECTOR)
public abstract class AbstractJmxConnectorTest<MBean> extends AbstractResourceConnectorTest {
    private final ObjectName beanName;
    protected final MBean beanInstance;
    protected static final String CONNECTOR_NAME = "jmx";
    public static final String JMX_LOGIN = "karaf";
    public static final String JMX_PASSWORD = "karaf";
    public static final int JMX_KARAF_PORT = 1099; // Located in KARAF_ROOT/etc/org.apache.karaf.management.cfg; property name is rmiRegistryPort
    protected static final String JMX_RMI_CONNECTION_STRING = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/karaf-root", JMX_KARAF_PORT);

    protected AbstractJmxConnectorTest(final MBean beanInstance, final ObjectName beanName){
        super(CONNECTOR_NAME, JMX_RMI_CONNECTION_STRING, ImmutableMap.of("login", JMX_LOGIN, "password", JMX_PASSWORD));
        this.beanName = beanName;
        this.beanInstance = beanInstance;
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        if(beanInstance instanceof PlatformManagedObject) return;
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if(mbs.isRegistered(beanName))
            mbs.unregisterMBean(beanName);
        mbs.registerMBean(beanInstance, beanName);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        if(beanInstance instanceof PlatformManagedObject) return;
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.unregisterMBean(beanName);
    }
}
