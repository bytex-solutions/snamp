package com.bytex.snamp.testing.connector.jmx;

import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * Represents a base class for JMX management connector tests with multiple instances of beans.
 * @author Evgeniy Kirichenki
 * @since 1.0
 */
@SnampDependencies(SnampFeature.JMX_CONNECTOR)
public abstract class AbstractMultiBeanJmxConnectorTest extends AbstractResourceConnectorTest {
    protected final Map<ObjectName, DynamicMBean> beanMap;
    protected static final String CONNECTOR_NAME = "jmx";
    public static final String JMX_LOGIN = "karaf";
    public static final String JMX_PASSWORD = "karaf";
    private static final int JMX_KARAF_PORT = 1099; // Located in KARAF_ROOT/etc/org.apache.karaf.management.cfg; property name is rmiRegistryPort
    private static final String JMX_RMI_CONNECTION_STRING = "service:jmx:rmi:///jndi/rmi://localhost:%s/karaf-root";

    protected AbstractMultiBeanJmxConnectorTest(final Map<ObjectName, DynamicMBean> map){
        super(CONNECTOR_NAME, getConnectionString(), ImmutableMap.of("login", JMX_LOGIN, "password", JMX_PASSWORD, "group", ""));
        this.beanMap = map;
    }

    public static String getConnectionString(){
        return getConnectionString(JMX_KARAF_PORT);
    }

    public static String getConnectionString(final int port){
        return String.format(JMX_RMI_CONNECTION_STRING, port);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        beanMap.entrySet().forEach(entry -> {
            try {
                if(mbs.isRegistered(entry.getKey())) {
                    mbs.unregisterMBean(entry.getKey());
                } else {
                    mbs.registerMBean(entry.getValue(), entry.getKey());
                }
            } catch (final InstanceNotFoundException | MBeanRegistrationException |
                            InstanceAlreadyExistsException | NotCompliantMBeanException e) {
                fail(e.getMessage());
            }
        });
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        beanMap.keySet().forEach(beanName -> {
            try {
                mbs.unregisterMBean(beanName);
            } catch (final InstanceNotFoundException | MBeanRegistrationException e) {
                fail(e.getMessage());
            }
        });
    }
}
