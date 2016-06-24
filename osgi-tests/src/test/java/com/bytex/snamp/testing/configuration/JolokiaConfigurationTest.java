package com.bytex.snamp.testing.configuration;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.google.common.base.Charsets;
import com.google.gson.*;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.jmx.json.JsonUtils.toJsonObject;
import static com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest.JMX_LOGIN;
import static com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest.JMX_PASSWORD;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class JolokiaConfigurationTest extends AbstractSnampIntegrationTest {
    private final Gson formatter = new Gson();
    private static final String SNAMP_CORE_MBEAN = "com.bytex.snamp.management:type=SnampCore";
    private static final URL JOLOKIA_URL = Utils.interfaceStaticInitialize(() -> new URL("http://localhost:8181/jolokia"));

    private static final class JolokiaAuthenticator extends Authenticator{
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(JMX_LOGIN, JMX_PASSWORD.toCharArray());
        }
    }

    private void writeAttribute(final String objectName,
                                final String attributeName,
                                final JsonElement attributeValue) throws IOException{
        //write attribute
        HttpURLConnection connection = (HttpURLConnection)JOLOKIA_URL.openConnection();
        connection.setRequestMethod("POST");
        Authenticator.setDefault(new JolokiaAuthenticator());
        connection.setDoOutput(true);
        final JsonObject request = toJsonObject("type", new JsonPrimitive("write"),
                "mbean", new JsonPrimitive(objectName),
                "attribute", new JsonPrimitive(attributeName),
                "value", attributeValue);
        IOUtils.writeString(formatter.toJson(request),
                connection.getOutputStream(),
                IOUtils.DEFAULT_CHARSET);
        connection.connect();
        try {
            assertEquals(200, connection.getResponseCode());
        }
        finally {
            connection.disconnect();
            Authenticator.setDefault(null);
        }
    }

    private JsonElement readAttribute(final String objectName,
                                             final String attributeName) throws IOException {

        //read attribute
        HttpURLConnection connection = (HttpURLConnection)JOLOKIA_URL.openConnection();
        connection.setRequestMethod("POST");
        Authenticator.setDefault(new JolokiaAuthenticator());
        connection.setDoOutput(true);
        final JsonObject request = toJsonObject("type", new JsonPrimitive("read"),
                "mbean", new JsonPrimitive(objectName),
                "attribute", new JsonPrimitive(attributeName),
                "maxDepth", new JsonPrimitive(20),
                "ignoreErrors", new JsonPrimitive(false));
        IOUtils.writeString(formatter.toJson(request),
                connection.getOutputStream(),
                IOUtils.DEFAULT_CHARSET);
        connection.connect();
        try{
            assertEquals(200, connection.getResponseCode());
            try(final InputStream is = connection.getInputStream();
                final Reader rdr = new InputStreamReader(is, IOUtils.DEFAULT_CHARSET)){
                return formatter.fromJson(rdr, JsonElement.class);
            }
        }
        finally {
            connection.disconnect();
            Authenticator.setDefault(null);
        }
    }

    @Test
    public void readConfigurationTest() throws IOException {
        JsonElement config =
                readAttribute(SNAMP_CORE_MBEAN, "configuration");
        assertTrue(config.isJsonObject());
        assertEquals(JsonNull.INSTANCE, config.getAsJsonObject().get("value"));
        //change config
        final String RESOURCE_NAME = "res1";
        processConfiguration(conf -> {
                final ManagedResourceConfiguration resource = conf.getEntities(ManagedResourceConfiguration.class).getOrAdd(RESOURCE_NAME);
                resource.getParameters().put("param1", "parameterValue");
                resource.setConnectionType("snmp");
                resource.setConnectionString("udp://127.0.0.1/161");
            return true;
            });
        config = readAttribute(SNAMP_CORE_MBEAN, "configuration");
        assertTrue(config.isJsonObject());
        config = config.getAsJsonObject().get("value");
        assertNotEquals(JsonNull.INSTANCE, config);
        //read managed resource
        config = config
                .getAsJsonObject().get("ManagedResources")
                .getAsJsonObject().get(RESOURCE_NAME)
                .getAsJsonObject().get("Connector")
                .getAsJsonObject().get("Parameters")
                .getAsJsonObject().get("param1")
                .getAsJsonObject().get("Value");
        assertEquals(new JsonPrimitive("parameterValue"), config);
    }

    @Test
    public void writeConfigurationTest() throws IOException{
        JsonElement config;
        try(final InputStream configStream =
                    getClass().getClassLoader().getResourceAsStream("TestConfiguration.json");
            final InputStreamReader reader = new InputStreamReader(configStream, Charsets.UTF_8)){
            config = formatter.fromJson(reader, JsonElement.class);
        }
        assertNotNull(config);
        assertNotEquals(JsonNull.INSTANCE, config);
        writeAttribute(SNAMP_CORE_MBEAN, "configuration", config);
        config = readAttribute(SNAMP_CORE_MBEAN, "configuration").getAsJsonObject().get("value");
        assertNotEquals(JsonNull.INSTANCE, config);
        //verify saved configuration
        JsonObject oidProperty = config
                .getAsJsonObject().get("ManagedResources")
                .getAsJsonObject().get("glassfish-v4")
                .getAsJsonObject().get("Connector")
                .getAsJsonObject().get("Attributes")
                .getAsJsonObject().get("memoryUsage")
                .getAsJsonObject().get("Attribute")
                .getAsJsonObject().get("AdditionalProperties")
                .getAsJsonObject().get("oid")
                .getAsJsonObject();
        assertEquals(new JsonPrimitive("1.1.6.1"), oidProperty.get("Value"));
        //change config
        oidProperty.addProperty("Value", "1.1.10.1");
        writeAttribute(SNAMP_CORE_MBEAN, "configuration", config);
        config = readAttribute(SNAMP_CORE_MBEAN, "configuration").getAsJsonObject().get("value");
        assertNotEquals(JsonNull.INSTANCE, config);
        //verify changed configuration
        oidProperty = config
                .getAsJsonObject().get("ManagedResources")
                .getAsJsonObject().get("glassfish-v4")
                .getAsJsonObject().get("Connector")
                .getAsJsonObject().get("Attributes")
                .getAsJsonObject().get("memoryUsage")
                .getAsJsonObject().get("Attribute")
                .getAsJsonObject().get("AdditionalProperties")
                .getAsJsonObject().get("oid")
                .getAsJsonObject();
        assertEquals(new JsonPrimitive("1.1.10.1"), oidProperty.get("Value"));
        //remove attribute
        assertNotNull(config
                .getAsJsonObject().get("ManagedResources")
                .getAsJsonObject().get("glassfish-v4")
                .getAsJsonObject().get("Connector")
                .getAsJsonObject().get("Attributes")
                .getAsJsonObject().remove("cpuLoad"));
        writeAttribute(SNAMP_CORE_MBEAN, "configuration", config);
        //verify changed configuration
        oidProperty = config
                .getAsJsonObject().get("ManagedResources")
                .getAsJsonObject().get("glassfish-v4")
                .getAsJsonObject().get("Connector")
                .getAsJsonObject().get("Attributes")
                .getAsJsonObject().get("memoryUsage")
                .getAsJsonObject().get("Attribute")
                .getAsJsonObject().get("AdditionalProperties")
                .getAsJsonObject().get("oid")
                .getAsJsonObject();
        assertEquals(new JsonPrimitive("1.1.10.1"), oidProperty.get("Value"));
        assertFalse(config
                .getAsJsonObject().get("ManagedResources")
                .getAsJsonObject().get("glassfish-v4")
                .getAsJsonObject().get("Connector")
                .getAsJsonObject().get("Attributes")
                .getAsJsonObject().has("cpuLoad"));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
    }
}