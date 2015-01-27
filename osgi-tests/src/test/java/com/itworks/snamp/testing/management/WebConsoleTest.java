package com.itworks.snamp.testing.management;

import com.google.gson.*;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.lang.management.ManagementFactory;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.SSH_ADAPTER, SnampFeature.JMX_CONNECTOR, SnampFeature.SNMP_CONNECTOR})

public final class WebConsoleTest extends AbstractSnampIntegrationTest {

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to set.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        ManagedResourceConfiguration resource = config.newConfigurationEntity(ManagedResourceConfiguration.class);
        config.getManagedResources().put("test-resource-1", resource);
        resource.getParameters().put("$param$", "value");
        final String jmxPort = System.getProperty("com.sun.management.jmxremote.port");
        final String connectionString = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi", jmxPort);
        resource.setConnectionString(connectionString);
        resource.setConnectionType("jmx");
        ManagedResourceConfiguration.AttributeConfiguration attr = resource.newElement(ManagedResourceConfiguration.AttributeConfiguration.class);
        attr.setAttributeName("simpleAttribute");
        attr.getParameters().put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
        attr.getParameters().put("useRegexp", "false");
        resource.getElements(ManagedResourceConfiguration.AttributeConfiguration.class).put("sa", attr);
        ResourceAdapterConfiguration adapter = config.newConfigurationEntity(ResourceAdapterConfiguration.class);
        adapter.setAdapterName("ssh");
        adapter.getParameters().put("port", "1212");
        config.getResourceAdapters().put("sshAdapter", adapter);
    }

    @Test
    public void discoverMetadataTest() {
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("roman", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/connectors/jmx/availableMetadata");
        final String jmxPort = System.getProperty("com.sun.management.jmxremote.port");
        final String connectionString = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi", jmxPort);
        final Gson serializer = new Gson();
        final String metadata = config
                .queryParam("connectionString", serializer.toJson(new JsonPrimitive(connectionString)))
                        .request()
                        .get(String.class);
        final JsonParser parser = new JsonParser();
        final JsonElement jsonMeta = parser.parse(metadata);
        assertTrue(jsonMeta.isJsonObject());
        assertTrue(jsonMeta.getAsJsonObject().has("attributes"));
        assertTrue(jsonMeta.getAsJsonObject().has("events"));
        assertTrue(jsonMeta.getAsJsonObject().getAsJsonArray("attributes").size() > 0);
        assertTrue(jsonMeta.getAsJsonObject().getAsJsonArray("events").size() > 0);
    }

    @Test
    public void suggestAdapterAttributeValues(){
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("roman", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/adapters/ssh/configurationSchema/host");
        final Gson serializer = new Gson();
        final String suggestedValues = config
                .queryParam("host", serializer.toJson(new JsonPrimitive("localhost")))
                .request()
                .get(String.class);
        assertNotNull(suggestedValues);
        assertFalse(suggestedValues.isEmpty());
        final JsonParser parser = new JsonParser();
        final JsonElement values = parser.parse(suggestedValues);
        assertTrue(values.isJsonArray());
        assertTrue(values.getAsJsonArray().size() > 0);
    }

    @Test
    public void suggestConnectorAttributeValues(){
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("roman", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/connectors/jmx/configurationSchema/attribute/objectName");
        final String jmxPort = System.getProperty("com.sun.management.jmxremote.port");
        final String connectionString = String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi", jmxPort);
        final Gson serializer = new Gson();
        final String suggestedValues = config
                .queryParam("connectionString", serializer.toJson(new JsonPrimitive(connectionString)))
                .request()
                .get(String.class);
        assertNotNull(suggestedValues);
        assertFalse(suggestedValues.isEmpty());
        final JsonParser parser = new JsonParser();
        final JsonElement values = parser.parse(suggestedValues);
        assertTrue(values.isJsonArray());
        assertTrue(values.getAsJsonArray().size() > 5);
    }

    @Test
    public void readLicenseFile(){
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("evgeniy", "mypassword"));
        final WebTarget licenseProvider = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/license");
        final String license = licenseProvider.request().get(String.class);
        assertNotNull(license);
        assertTrue(license.contains("jmxConnectorLimitations"));
    }

    @Test
    public void writeLicenseFile(){
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("evgeniy", "mypassword"));
        final WebTarget licenseProvider = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/license");
        final String originalContent = licenseProvider.request().get(String.class);
        final String LICENSE_CONTENT = "INCORRECT LICENSE";
        try{
            licenseProvider.request(MediaType.APPLICATION_XML_TYPE).post(Entity.xml(LICENSE_CONTENT));
            final String newContent = licenseProvider.request().get(String.class);
            assertEquals(LICENSE_CONTENT, newContent);
        }
        finally {
            licenseProvider.request(MediaType.APPLICATION_XML_TYPE).post(Entity.xml(originalContent));
        }
    }

    @Test
    public void readConfigurationTest() throws InterruptedException {
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("roman", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/configuration");
        final String configJson = config.request().get(String.class);
        assertNotNull(configJson);
        //parse response and check validity of JSON document
        final JsonParser parser = new JsonParser();
        final JsonElement element = parser.parse(configJson);
        assertTrue(element.isJsonObject());
        assertEquals(2, element.getAsJsonObject().entrySet().size());
        assertTrue(element.getAsJsonObject().has("resourceAdapters"));
        assertTrue(element.getAsJsonObject().has("managedResources"));
        assertEquals(1, element.getAsJsonObject().get("resourceAdapters").getAsJsonObject().entrySet().size());
        assertEquals(1, element.getAsJsonObject().get("managedResources").getAsJsonObject().entrySet().size());
    }

    @Test
    public void writeConfigurationTest(){
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("evgeniy", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/configuration");
        final Gson serializer = new Gson();
        final JsonObject newConfig = new JsonObject();
        newConfig.add("resourceAdapters", new JsonObject());
        newConfig.add("managedResources", new JsonObject());
        config.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(serializer.toJson(newConfig)));
        //parse response and check validity of JSON document
        final String configJson = config.request().get(String.class);
        final JsonParser parser = new JsonParser();
        final JsonElement element = parser.parse(configJson);
        assertTrue(element.isJsonObject());
        assertEquals(2, element.getAsJsonObject().entrySet().size());
        assertTrue(element.getAsJsonObject().has("resourceAdapters"));
        assertTrue(element.getAsJsonObject().has("managedResources"));
        assertEquals(0, element.getAsJsonObject().get("resourceAdapters").getAsJsonObject().entrySet().size());
        assertEquals(0, element.getAsJsonObject().get("managedResources").getAsJsonObject().entrySet().size());
    }

    @Test
    public void jmxConnectorConfigurationSchema(){
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("evgeniy", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/connectors/jmx/configurationSchema");
        final JsonParser parser = new JsonParser();
        final JsonElement schema = parser.parse(config.request().get(String.class));
        assertTrue(schema.isJsonObject());
        assertEquals(4, schema.getAsJsonObject().entrySet().size());
        assertTrue(schema.getAsJsonObject().get("attributeParameters").getAsJsonObject().has("objectName"));
    }

    @Test
    public void listOfConnectorsTest(){
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("evgeniy", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/connectors");
        final JsonParser parser = new JsonParser();
        final JsonElement connectors = parser.parse(config.request().get(String.class));
        assertTrue(connectors.isJsonArray());
        assertEquals(2, connectors.getAsJsonArray().size());
    }

    @Test
    public void listOfComponentsTest(){
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("evgeniy", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/api/components");
        final JsonParser parser = new JsonParser();
        final JsonElement connectors = parser.parse(config.request().get(String.class));
        assertTrue(connectors.isJsonArray());
        assertTrue(connectors.getAsJsonArray().size() > 4);
    }

    @Test
    public void requestMainPageTest(){
        final Client webConsoleClient = ClientBuilder.newClient()
                .register(HttpAuthenticationFeature.digest("roman", "mypassword"));
        final WebTarget config = webConsoleClient.target("http://127.0.0.1:3344/snamp/management/console/");
        final String pageContent = config.request().get(String.class);
        assertTrue(pageContent.contains("<html"));
    }
}


