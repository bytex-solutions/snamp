package com.itworks.snamp.testing.management;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.testing.SnampArtifact;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.junit.Test;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collection;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WebConsoleTest extends AbstractSnampIntegrationTest {
    public WebConsoleTest(){
        super(mavenBundle("org.eclipse.jetty", "jetty-xml", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-security", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-io", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-http", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-util", "9.1.1.v20140108"),
                mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0"),
                mavenBundle("org.eclipse.jetty", "jetty-server", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-webapp", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-servlet", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-servlet", "9.1.1.v20140108"),
                mavenBundle("com.sun.jersey", "jersey-core", "1.17.1"),
                mavenBundle("com.sun.jersey", "jersey-server", "1.17.1"),
                mavenBundle("com.sun.jersey", "jersey-servlet", "1.17.1"),
                mavenBundle("com.sun.jersey", "jersey-client", "1.17.1"),
                mavenBundle("com.google.code.gson", "gson", "2.2.4"),
                SnampArtifact.MANAGEMENT.getReference(),
                SnampArtifact.WEB_CONSOLE.getReference());
    }

    @Override
    protected Collection<FrameworkPropertyOption> getFrameworkProperties() {
        return Arrays.asList(
                frameworkProperty("com.itworks.snamp.webconsole.port").value("3344"),
                frameworkProperty("com.itworks.snamp.webconsole.host").value("localhost"));
    }

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
        resource.setConnectionString("connection-string");
        resource.setConnectionType("JMX");
        ManagedResourceConfiguration.AttributeConfiguration attr = resource.newElement(ManagedResourceConfiguration.AttributeConfiguration.class);
        attr.setAttributeName("simpleAttribute");
        resource.getElements(ManagedResourceConfiguration.AttributeConfiguration.class).put("sa", attr);
        ResourceAdapterConfiguration adapter = config.newConfigurationEntity(ResourceAdapterConfiguration.class);
        adapter.setAdapterName("SNMP");
        adapter.getHostingParams().put("port", "1212");
        config.getResourceAdapters().put("s", adapter);
    }

    @Test
    public void readLicenseFile(){
        final Client webConsoleClient = new Client();
        final WebResource licenseProvider = webConsoleClient.resource("http://127.0.0.1:3344/snamp/console/license");
        final String license = licenseProvider.get(String.class);
        assertNotNull(license);
        assertTrue(license.contains("jmxConnectorLimitations"));
    }

    @Test
    public void writeLicenseFile(){
        final Client webConsoleClient = new Client();
        final WebResource licenseProvider = webConsoleClient.resource("http://127.0.0.1:3344/snamp/console/license");
        final String originalContent = licenseProvider.get(String.class);
        final String LICENSE_CONTENT = "INCORRECT LICENSE";
        try{
            licenseProvider.getRequestBuilder().type(MediaType.APPLICATION_XML_TYPE).post(LICENSE_CONTENT);
            final String newContent = licenseProvider.get(String.class);
            assertEquals(LICENSE_CONTENT, newContent);
        }
        finally {
            licenseProvider.getRequestBuilder().type(MediaType.APPLICATION_XML_TYPE).post(originalContent);
        }
    }

    @Test
    public void readConfigurationTest() {
        final Client webConsoleClient = new Client();
        final WebResource config = webConsoleClient.resource("http://127.0.0.1:3344/snamp/console/configuration");
        final String configJson = config.get(String.class);
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
        final Client webConsoleClient = new Client();
        final WebResource config = webConsoleClient.resource("http://127.0.0.1:3344/snamp/console/configuration");
        final Gson serializer = new Gson();
        final JsonObject newConfig = new JsonObject();
        newConfig.add("resourceAdapters", new JsonObject());
        newConfig.add("managedResources", new JsonObject());
        config.getRequestBuilder().
                type(MediaType.APPLICATION_JSON_TYPE).post(serializer.toJson(newConfig));
        //parse response and check validity of JSON document
        final String configJson = config.get(String.class);
        final JsonParser parser = new JsonParser();
        final JsonElement element = parser.parse(configJson);
        assertTrue(element.isJsonObject());
        assertEquals(2, element.getAsJsonObject().entrySet().size());
        assertTrue(element.getAsJsonObject().has("resourceAdapters"));
        assertTrue(element.getAsJsonObject().has("managedResources"));
        assertEquals(0, element.getAsJsonObject().get("resourceAdapters").getAsJsonObject().entrySet().size());
        assertEquals(0, element.getAsJsonObject().get("managedResources").getAsJsonObject().entrySet().size());
    }
}
