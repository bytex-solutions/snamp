package com.itworks.snamp.testing.adapters.snmp;

import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestManagementBean;
import org.apache.commons.collections4.Factory;
import org.junit.Test;
import org.snmp4j.smi.OID;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.testing.connectors.jmx.TestManagementBean.BEAN_NAME;

/**
 * Represents integration tests for JMX resource connector and SNMP resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxToSnmpV2Test extends AbstractJmxConnectorTest<TestManagementBean> {
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final SnmpClient client = SnmpClientFactory.createSnmpV2("udp:" + SNMP_HOST + "/" + SNMP_PORT);

    public JmxToSnmpV2Test() throws MalformedObjectNameException {
        super(new TestManagementBean(), new ObjectName(BEAN_NAME),
                SnampArtifact.SNMP4J.getReference(),
                SnampArtifact.SNMP_ADAPTER.getReference());
    }

    @Test
    public final void testForStringProperty() throws IOException {
        final String valueToCheck = "SETTED VALUE";
        final OID attributeId = new OID("1.1.1.0");
        client.writeAttribute(attributeId, valueToCheck, String.class);
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, attributeId, String.class));
        assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, attributeId, String.class));
    }

    @Override
    protected void fillAdapters(final Map<String, ResourceAdapterConfiguration> adapters, final Factory<ResourceAdapterConfiguration> adapterFactory) {
        final ResourceAdapterConfiguration snmpAdapter = adapterFactory.create();
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getHostingParams().put("port", SNMP_PORT);
        snmpAdapter.getHostingParams().put("host", SNMP_HOST);
        snmpAdapter.getHostingParams().put("socketTimeout", "5000");
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Factory<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.create();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.1.0");
        attributes.put("1.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.2.0");
        attributes.put("2.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.3.0");
        attributes.put("3.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.4.0");
        attributes.put("4.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.5.1");
        attributes.put("5.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.6.1");
        attributes.put("6.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.7.1");
        attributes.put("7.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.8.0");
        attributes.put("8.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.getParameters().put("oid", "1.1.9.0");
        attributes.put("9.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903-human-readable");
        attribute.getParameters().put("oid", "1.1.10.0");
        attributes.put("10.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903");
        attribute.getParameters().put("oid", "1.1.11.0");
        attributes.put("11.0", attribute);
    }
}
