package com.snamp.adapters;

import com.google.gson.Gson;
import com.snamp.connectors.*;
import static com.snamp.hosting.EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.ws.rs.core.MediaType;

import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author roman
 */
public final class RestAdapterTest extends JmxConnectorTest<TestManagementBean> {
    private static final Map<String, String> restAdapterSettings = new HashMap<String, String>(2){{
        put(Adapter.portParamName, "3222");
        put(Adapter.addressParamName, "127.0.0.1");
    }};
    private static final String BEAN_NAME = "com.snampy.jmx:type=com.snamp.adapters.TestManagementBean";

    private final Gson jsonFormatter;

    public RestAdapterTest() throws MalformedObjectNameException {
        super("rest", restAdapterSettings, new TestManagementBean(), new ObjectName(BEAN_NAME));
        jsonFormatter = new Gson();
    }

    @Override
    protected String getAttributesNamespace() {
        return "test";
    }

    private URL buildAttributeGetter(final String postfix) throws MalformedURLException {
        return new URL(String.format("http://%s:%s/snamp/management/attribute/%s/%s", restAdapterSettings.get(Adapter.addressParamName), restAdapterSettings.get(Adapter.portParamName), getAttributesNamespace(), postfix));
    }

    private String readAttribute(final String postfix) throws IOException {
        final URL attributeGetter = buildAttributeGetter(postfix);
        final URLConnection connection = attributeGetter.openConnection();
        connection.connect();
        assertEquals(MediaType.APPLICATION_JSON, connection.getContentType());
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
            String line = null;
            final StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) result.append(line);
            return result.toString();
        }
    }

    private <T> T readAttribute(final String postfix, final Class<T> attributeType) throws IOException {
        return jsonFormatter.fromJson(readAttribute(postfix), attributeType);
    }

    @Test
    public final void testForStringProperty() throws IOException {
        //Thread.sleep(Long.MAX_VALUE);
        assertEquals("NO VALUE", readAttribute("stringProperty", String.class));
    }

    @Override
    protected final void fillAttributes(final Map<String, AttributeConfiguration> attributes) {
        EmbeddedAttributeConfiguration attribute = new EmbeddedAttributeConfiguration("string");
        attribute.getAdditionalElements().put("objectName", BEAN_NAME);
        attributes.put("stringProperty", attribute);
    }
}
