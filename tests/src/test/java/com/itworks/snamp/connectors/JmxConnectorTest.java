package com.itworks.snamp.connectors;

import com.itworks.snamp.hosting.Agent;
import com.itworks.snamp.hosting.*;

import javax.management.*;
import java.lang.management.*;
import static com.itworks.snamp.configuration.EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;
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
    protected void beforeAgentStart(final Agent agent) throws Exception{
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if(mbs.isRegistered(beanName))
            mbs.unregisterMBean(beanName);
        mbs.registerMBean(bean, beanName);
    }

    @Override
    protected void afterAgentStop() throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.unregisterMBean(beanName);
    }

    protected String getAttributesNamespace(){
        return "";
    }

    protected abstract void fillAttributes(final Map<String, AttributeConfiguration> attributes);

    protected void fillEvents(final Map<String, EventConfiguration> events){

    }

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
            fillEvents(targetConfig.getEvents());
            put("test-jmx", targetConfig);
        }};
    }
}
