package com.itworks.snamp.configuration;

import com.itworks.snamp.SnampClassTestSet;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.xml.XmlAgentConfiguration;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XmlAgentConfigurationTest extends SnampClassTestSet<XmlAgentConfiguration> {
    @Test
    public final void serializationTest() throws IOException {
        final XmlAgentConfiguration config = new XmlAgentConfiguration();
        config.getAgentHostingConfig().setAdapterName("snmp");
        config.getAgentHostingConfig().getHostingParams().put("port", "22");
        config.getAgentHostingConfig().getHostingParams().put("address", "localhost");
        final AgentConfiguration.ManagementTargetConfiguration target = config.newManagementTargetConfiguration();
        config.getTargets().put("test-server", target);
        target.setConnectionString("http://connection-string");
        target.setConnectionType("jmx");
        final AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration attr = target.newAttributeConfiguration();
        attr.setAttributeName("attribute1");
        attr.setReadWriteTimeout(new TimeSpan(100));
        target.getAttributes().put("test-attribute", attr);
        final String content = config.toXmlString();
        final XmlAgentConfiguration newConfig = new XmlAgentConfiguration();
        newConfig.fromXmlString(content);
        assertTrue(XmlAgentConfiguration.equals(newConfig, config));
    }
}
