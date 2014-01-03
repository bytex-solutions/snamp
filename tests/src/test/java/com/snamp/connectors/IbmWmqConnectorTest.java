package com.snamp.connectors;

import com.snamp.SnampClassTestSet;
import com.snamp.TimeSpan;
import com.snamp.hosting.Agent;
import com.snamp.hosting.AgentConfiguration;
import com.snamp.hosting.EmbeddedAgentConfiguration;
import com.snamp.hosting.HostingTest;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents tests for {@link ManagementConnectorBean} class.
 * @author Roman Sakno
 */
public abstract class IbmWmqConnectorTest extends HostingTest {
    private static final String CONNECTOR_NAME = "ibm-wmq";

    @Override
    final protected void beforeAgentStart(final Agent agent) throws Exception {
    }

    @Override
    final protected void afterAgentStart(final Agent agent) throws Exception {
    }

    @Override
    final protected void beforeAgentStop(final Agent agent) throws Exception {
    }

    @Override
    final protected void afterAgentStop(final Agent agent) throws Exception {
    }

    protected IbmWmqConnectorTest(final String adapterName, final Map<String, String> adapterParams) {
        super(adapterName, adapterParams);
    }

    protected String getAttributesNamespace(){
        return "test";
    }

    @Override
    public Map<String, ManagementTargetConfiguration> getTargets() {
        return new HashMap<String, ManagementTargetConfiguration>(1){{
            final ManagementTargetConfiguration targetConfig = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration();
            targetConfig.setConnectionString("wmq://SYSTEM.BKR.CONFIG@anticitizen.dhis.org:8000/TEST_QMGR");
            targetConfig.setConnectionType(CONNECTOR_NAME);
            targetConfig.setNamespace(getAttributesNamespace());
            fillAttributes(targetConfig.getAttributes());
            fillEvents(targetConfig.getEvents());
            put("test-ibm-wmq", targetConfig);
        }};
    }

    protected abstract void fillAttributes(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> attributes);

    protected void fillEvents(final Map<String, ManagementTargetConfiguration.EventConfiguration> events){

    }
}
