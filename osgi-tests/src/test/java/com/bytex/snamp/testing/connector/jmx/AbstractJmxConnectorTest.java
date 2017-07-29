package com.bytex.snamp.testing.connector.jmx;

import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;

/**
 * Represents a base class for JMX management connector tests.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@SnampDependencies(SnampFeature.JMX_CONNECTOR)
public abstract class AbstractJmxConnectorTest<MBean> extends AbstractResourceConnectorTest {
    private final ObjectName beanName;
    private final MBean beanInstance;
    protected static final String CONNECTOR_NAME = "jmx";
    public static final String JMX_LOGIN = "karaf";
    public static final String JMX_PASSWORD = "karaf";
    private static final int JMX_KARAF_PORT = 1099; // Located in KARAF_ROOT/etc/org.apache.karaf.management.cfg; property name is rmiRegistryPort
    private static final String JMX_RMI_CONNECTION_STRING = "service:jmx:rmi:///jndi/rmi://localhost:%s/karaf-root";
    public static final ImmutableMap<String, String> DEFAULT_PARAMS = ImmutableMap.of("login", JMX_LOGIN, "password", JMX_PASSWORD);

    protected AbstractJmxConnectorTest(final MBean beanInstance, final ObjectName beanName){
        super(CONNECTOR_NAME, getConnectionString(), DEFAULT_PARAMS);
        this.beanName = beanName;
        this.beanInstance = beanInstance;
    }

    public static String getConnectionString(){
        return getConnectionString(JMX_KARAF_PORT);
    }

    public static String getConnectionString(final int port){
        return String.format(JMX_RMI_CONNECTION_STRING, port);
    }

    public static void beforeStartTest(final ObjectName beanName,
                                final Object beanInstance) throws JMException {
        if(beanInstance instanceof PlatformManagedObject) return;
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if(mbs.isRegistered(beanName))
            mbs.unregisterMBean(beanName);
        mbs.registerMBean(beanInstance, beanName);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        beforeStartTest(beanName, beanInstance);
    }

    public static void afterCleanupTest(final ObjectName beanName,
                                 final Object beanInstance) throws JMException{
        if(beanInstance instanceof PlatformManagedObject) return;
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.unregisterMBean(beanName);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        afterCleanupTest(beanName, beanInstance);
    }
}
