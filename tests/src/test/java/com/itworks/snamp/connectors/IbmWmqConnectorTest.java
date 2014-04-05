package com.itworks.snamp.connectors;

import com.itworks.snamp.configuration.EmbeddedAgentConfiguration;
import com.itworks.snamp.hosting.HostingTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents tests for {@link com.itworks.snamp.connectors.ManagementConnectorBean} class.
 * @author Roman Sakno
 */
public abstract class IbmWmqConnectorTest extends HostingTest {
    private static final String CONNECTOR_NAME = "ibm-wmq";


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
