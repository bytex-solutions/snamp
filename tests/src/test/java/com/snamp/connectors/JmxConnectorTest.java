package com.snamp.connectors;

import com.snamp.hosting.*;
import org.junit.*;

import javax.management.*;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.*;
import static com.snamp.hosting.EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

import java.net.MalformedURLException;
import java.util.*;

/**
 * @author Roman Sakno
 */
public abstract class JmxConnectorTest<ManagementBean> extends HostingTest {
    private final ManagementBean bean;
    private final ObjectName beanName;

    protected JmxConnectorTest(final String adapterName, final Map<String, String> adapterParams, final ManagementBean bean, final ObjectName beanName){
        super(adapterName, adapterParams);
        this.bean = bean;
        this.beanName = beanName;
    }

    @Override
    protected final void beforeAgentStart(final Agent agent) throws JMException{
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if(mbs.isRegistered(beanName))
            mbs.unregisterMBean(beanName);
        mbs.registerMBean(bean, beanName);
    }

    @Override
    protected final void afterAgentStop(final Agent agent) throws JMException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.unregisterMBean(beanName);
    }

    protected String getAttributesNamespace(){
        return "";
    }

    protected abstract void fillAttributes(final Map<String, AttributeConfiguration> attributes);

    /**
     * Represents management targets.
     *
     * @return The dictionary of management targets (management back-ends).
     */
    @Override
    public final Map<String, ManagementTargetConfiguration> getTargets() {
        return new HashMap<String, ManagementTargetConfiguration>(1){{
            final ManagementTargetConfiguration targetConfig = new EmbeddedManagementTargetConfiguration();
            final String localJMXPort = System.getProperty("com.sun.management.jmxremote.port", "9010");
            targetConfig.setConnectionString(String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi", localJMXPort));
            targetConfig.setConnectionType("jmx");
            targetConfig.setNamespace(getAttributesNamespace());
            fillAttributes(targetConfig.getAttributes());
            put("test-jmx", targetConfig);
        }};
    }
}
