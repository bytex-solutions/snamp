package com.bytex.snamp.testing.management;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.json.ThreadLocalJsonFactory;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.bytex.snamp.testing.web.TestAuthenticator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;


/**
 * The Snamp webconsole test.
 *
 * @author Evgeniy Kirichenko.
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies({
        SnampFeature.SNMP_GATEWAY,
        SnampFeature.STANDARD_TOOLS
})
public final class HttpManagementTest extends AbstractJmxConnectorTest<TestOpenMBean> {
    private final ObjectMapper mapper;
    private static final String ADAPTER_INSTANCE_NAME = "test-snmp";
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final String TEST_PARAMETER = "testParameter";

    private final TestAuthenticator authenticator;

    /**
     * Instantiates a new Snamp webconsole test.
     *
     * @throws MalformedObjectNameException the malformed object name exception
     */
    public HttpManagementTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
        authenticator = new TestAuthenticator(getTestBundleContext());
        mapper = new ObjectMapper();
    }

    /**
     * Login with valid credentials.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testLoginValidCredentials() throws IOException, GeneralSecurityException, InterruptedException {
        final HttpCookie authCookie = authenticator.authenticateTestUser();
        assertNotNull(authCookie);
        authenticator.verifyTestUser(authCookie.getValue());
    }

    /**
     * Login with invalid credentials.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoginInvalidCredentials() throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        authenticator.authenticateClient(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    /**
     * Test check simple resource with and without token.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testCheckSimpleResourceWithAndWithoutToken() throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticator.authenticateTestUser();
        final URL query = new URL("http://localhost:8181/snamp/security/login/username");
        //with token
        HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(String.format("Wrong response code (%s) received on the authentication phase",
                    connection.getResponseCode()), HttpURLConnection.HTTP_OK, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

        connection = (HttpURLConnection)query.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.connect();
        try {
            assertEquals(String.format("Wrong response code (%s) received on the authentication phase",
                    connection.getResponseCode()), HttpURLConnection.HTTP_UNAUTHORIZED, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testFullConfiguration() throws IOException {
        final HttpCookie cookie = authenticator.authenticateTestUser();

        // Get full configuration
        final URL query = new URL("http://localhost:8181/snamp/management/configuration/");
        final HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            final String configuration = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotEquals("{}", configuration);
        } finally {
            connection.disconnect();
        }
    }

    private JsonNode discoverFeatures(final String features) throws IOException{
        final HttpCookie cookie = authenticator.authenticateTestUser();

        // Get full configuration
        final URL query = new URL("http://localhost:8181/snamp/management/configuration/resource/" + TEST_RESOURCE_NAME + "/discovery/" + features);
        final HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try{
            return mapper.readValue(IOUtils.toString(connection.getInputStream(), Charset.defaultCharset()), JsonNode.class);
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void discoveryTest() throws IOException{
        JsonNode attributes = discoverFeatures("attributes");
        assertNotNull(attributes);
    }

    /**
     * Test check simple resource with and without token.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testGetResourceConfiguration() throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticator.authenticateTestUser();

        // Get all resources
        URL query = new URL("http://localhost:8181/snamp/management/configuration/resource");
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotEquals("{}", attributeValue);
        } finally {
            connection.disconnect();
        }

        // Get resource by name
        query = new URL(String.format("http://localhost:8181/snamp/management/configuration/resource/%s", TEST_RESOURCE_NAME));
        //write attribute
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotEquals("{}", attributeValue);
        } finally {
            connection.disconnect();
        }

        // Get resource by name
        query = new URL(String.format("http://localhost:8181/snamp/management/configuration/resource/%s/attributes/1.0", TEST_RESOURCE_NAME));
        //write attribute
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotEquals("{}", attributeValue);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Test check simple resource with and without token.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testModifyResourceConfiguration() throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticator.authenticateTestUser();
        // Get resource by name
        URL query = new URL(String.format("http://localhost:8181/snamp/management/configuration/resource/%s", TEST_RESOURCE_NAME));
        HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();

        String responseValue;
        try {
            responseValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(responseValue);
            assertNotEquals("{}", responseValue);
        } finally {
            connection.disconnect();
        }
        //write configuration for certain resource
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setDoOutput(true);
        IOUtils.writeString(responseValue, connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_NO_CONTENT, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

        //read configuration - nothing should be different
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String newConfiguration = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            JsonNode oldResponseJSON = mapper.readTree(responseValue);
            JsonNode newResponseJSON = mapper.readTree(newConfiguration);
            assertEquals(oldResponseJSON, newResponseJSON);
        } finally {
            connection.disconnect();
        }

        // append some param
        connection = (HttpURLConnection) new URL(String.format("http://localhost:8181/snamp/management/configuration/resource/%s/parameters/%s",
                TEST_RESOURCE_NAME, "dummyParam")).openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.setDoOutput(true);
        IOUtils.writeString("dummyValue", connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_NO_CONTENT, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

        //read configuration - new "extra" param with name "dummy" should present
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String newConfiguration = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            JsonNode oldResponseJSON = mapper.readTree(responseValue);
            JsonNode newResponseJSON = mapper.readTree(newConfiguration);
            ((ObjectNode)oldResponseJSON.get("parameters")).put("dummyParam", "dummyValue");

            assertEquals(oldResponseJSON, newResponseJSON);
        } finally {
            connection.disconnect();
        }

        // remove parameter
        connection = (HttpURLConnection) new URL(String.format("http://localhost:8181/snamp/management/configuration/resource/%s/parameters/%s",
                TEST_RESOURCE_NAME, "dummyParam")).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_NO_CONTENT, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

        // check if removed parameter was successfully removed
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String newConfiguration = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            JsonNode oldResponseJSON = mapper.readTree(responseValue);
            JsonNode newResponseJSON = mapper.readTree(newConfiguration);
            assertEquals(oldResponseJSON, newResponseJSON);
        } finally {
            connection.disconnect();
        }

        // append some attribute
        connection = (HttpURLConnection) new URL(String.format("http://localhost:8181/snamp/management/configuration/resource/%s/attributes",
                TEST_RESOURCE_NAME)).openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setDoOutput(true);

        final ObjectNode attributeMap = ThreadLocalJsonFactory.getFactory().objectNode();
        final ObjectNode newAttribute1 = ThreadLocalJsonFactory.getFactory().objectNode();
        final ObjectNode newAttribute1Params = ThreadLocalJsonFactory.getFactory().objectNode();
        newAttribute1Params.put("name", new TextNode("string"));
        newAttribute1Params.put("objectName", new TextNode(BEAN_NAME));
        newAttribute1Params.put("oid", new TextNode("1.1.123.0"));
        newAttribute1.put("parameters", newAttribute1Params);
        newAttribute1.put("readWriteTimeout", ThreadLocalJsonFactory.getFactory().nullNode());

        final ObjectNode newAttribute2 = ThreadLocalJsonFactory.getFactory().objectNode();
        final ObjectNode newAttribute2Params = ThreadLocalJsonFactory.getFactory().objectNode();
        newAttribute2Params.put("name", new TextNode("string"));
        newAttribute2Params.put("objectName", new TextNode(BEAN_NAME));
        newAttribute2Params.put("oid", new TextNode("1.1.124.0"));
        newAttribute2.put("parameters", newAttribute2Params);
        newAttribute2.put("readWriteTimeout", ThreadLocalJsonFactory.getFactory().nullNode());

        final ObjectNode newAttribute3 = ThreadLocalJsonFactory.getFactory().objectNode();
        final ObjectNode newAttribute3Params = ThreadLocalJsonFactory.getFactory().objectNode();
        newAttribute3Params.put("name", new TextNode("string"));
        newAttribute3Params.put("objectName", new TextNode(BEAN_NAME));
        newAttribute3Params.put("oid", new TextNode("1.1.125.0"));
        newAttribute3.put("parameters", newAttribute3Params);
        newAttribute3.put("readWriteTimeout", ThreadLocalJsonFactory.getFactory().nullNode());

        attributeMap.put("123.0", newAttribute1);
        attributeMap.put("124.0", newAttribute2);
        attributeMap.put("125.0", newAttribute3);

        IOUtils.writeString(attributeMap.toString(), connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_NO_CONTENT, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

        // check if we replaced old attributes with new once
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String newConfiguration = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            ObjectNode oldResponseJSON = (ObjectNode) mapper.readTree(responseValue);
            JsonNode newResponseJSON = mapper.readTree(newConfiguration);
            oldResponseJSON.put("attributes", attributeMap);
            assertEquals(newResponseJSON, oldResponseJSON);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Test check simple resource with and without token.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testModifyGatewayConfiguration() throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticator.authenticateTestUser();
        // Get resource by name
        URL query = new URL(String.format("http://localhost:8181/snamp/management/configuration/gateway/%s", ADAPTER_INSTANCE_NAME));
        HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();

        String responseValue;
        try {
            responseValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(responseValue);
            assertNotEquals("{}", responseValue);
        } finally {
            connection.disconnect();
        }
        //write configuration for certain resource
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setDoOutput(true);
        IOUtils.writeString(responseValue, connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_NO_CONTENT, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

        //read configuration - nothing should be different
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String newConfiguration = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            JsonNode oldResponseJSON = mapper.readTree(responseValue);
            JsonNode newResponseJSON = mapper.readTree(newConfiguration);

            assertEquals(oldResponseJSON, newResponseJSON);
        } finally {
            connection.disconnect();
        }

        // remove parameter
        connection = (HttpURLConnection) new URL(String.format("http://localhost:8181/snamp/management/configuration/gateway/%s/parameters/%s", ADAPTER_INSTANCE_NAME, TEST_PARAMETER)).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_NO_CONTENT, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

        //read configuration - nothing should be different
        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String newConfiguration = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            JsonNode oldResponseJSON = mapper.readTree(responseValue);
            ((ObjectNode)oldResponseJSON.get("parameters")).remove(TEST_PARAMETER);
            JsonNode newResponseJSON = mapper.readTree(newConfiguration);
            assertEquals(oldResponseJSON, newResponseJSON);
        } finally {
            connection.disconnect();
        }

        // get some bindings
        connection = (HttpURLConnection) new URL(String.format("http://localhost:8181/snamp/management/configuration/gateway/%s/attributes/bindings",
                ADAPTER_INSTANCE_NAME)).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertNotNull(IOUtils.toString(connection.getInputStream(), Charset.defaultCharset()));
        } finally {
            connection.disconnect();
        }

        // get the configuration
        connection = (HttpURLConnection) new URL(String.format("http://localhost:8181/snamp/management/gateway/%s/configuration",
                ADAPTER_NAME)).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertNotNull(IOUtils.toString(connection.getInputStream(), Charset.defaultCharset()));
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Test check simple resource with and without token.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testManagementService() throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticator.authenticateTestUser();

        // Get all resources
        URL query = new URL("http://localhost:8181/snamp/management/components");
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            final String componentString = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(componentString);
            assertNotEquals("{}", componentString);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Test check simple resource with and without token.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testDisableAndEnableComponents() throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticator.authenticateTestUser();

        // Get all resources
        URL query = new URL("http://localhost:8181/snamp/management/resource/list");
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            final String responseValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(responseValue);
            final JsonNode resources = mapper.readTree(responseValue);

            final Optional<JsonNode> element = StreamSupport.stream(resources.spliterator(), false)
                    .filter(entry -> entry.get("name").asText().equalsIgnoreCase("JMX Connector"))
                    .findFirst();
            assertTrue(element.isPresent());
            assertEquals("ACTIVE", element.get().get("state").asText());

        } finally {
            connection.disconnect();
        }

        connection = (HttpURLConnection) new URL(String.format("http://localhost:8181/snamp/management/resource/%s/disable",
                CONNECTOR_NAME)).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertTrue(Boolean.valueOf(IOUtils.toString(connection.getInputStream(), Charset.defaultCharset())));

        } finally {
            connection.disconnect();
        }

        connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            final String responseValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(responseValue);
            final JsonNode resources = mapper.readTree(responseValue);

            final Optional<JsonNode> element = StreamSupport.stream(resources.spliterator(), false)
                    .filter(entry -> entry.get("name").asText().equalsIgnoreCase("JMX Connector"))
                    .findFirst();
            assertTrue(element.isPresent());
            assertEquals("RESOLVED", element.get().get("state").asText());

        } finally {
            connection.disconnect();
        }

    }


    /**
     * Test attributes bindings.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testAttributesBindings() throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticator.authenticateTestUser();

        // Get all resources
        URL query = new URL(String.format("http://localhost:8181/snamp/management/configuration/gateway/%s/attributes/bindings", ADAPTER_INSTANCE_NAME));
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            final String componentString = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(componentString);
            assertNotEquals("{}", componentString);
        } finally {
            connection.disconnect();
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(ADAPTER_NAME, () -> {
            GatewayActivator.enableGateway(context, ADAPTER_NAME);
            return null;
        }, Duration.ofSeconds(30));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        final GatewayConfiguration snmpAdapter = gateways.getOrAdd(ADAPTER_INSTANCE_NAME);
        snmpAdapter.setType(ADAPTER_NAME);
        snmpAdapter.put("port", SNMP_PORT);
        snmpAdapter.put("host", SNMP_HOST);
        snmpAdapter.put(TEST_PARAMETER, "parameter");
        snmpAdapter.put("socketTimeout", "5000");
        snmpAdapter.put("context", "1.1");

        // second instance of gateways for better console default content (dummyTest support)
        final GatewayConfiguration snmpAdapterDummy = gateways.getOrAdd("new_snmp_adapter");
        snmpAdapterDummy.setType(ADAPTER_NAME);
        snmpAdapterDummy.put("port", "3232");
        snmpAdapterDummy.put("host", SNMP_HOST);
        snmpAdapterDummy.put(TEST_PARAMETER, "parameter");
        snmpAdapterDummy.put("socketTimeout", "5000");
        snmpAdapterDummy.put("context", "1.2");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.1.0");

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.2.0");

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.3.0");

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.4.0");

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.5.1");

        attribute = attributes.getOrAdd("6.1");
        attribute.setAlternativeName("dictionary");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.6.1");

        attribute = attributes.getOrAdd("7.1");
        attribute.setAlternativeName("table");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.7.1");

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("oid", "1.1.8.0");

        attribute = attributes.getOrAdd("9.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.put("oid", "1.1.9.0");

        attribute = attributes.getOrAdd("10.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("displayFormat", "rfc1903-human-readable");
        attribute.put("oid", "1.1.10.0");

        attribute = attributes.getOrAdd("11.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", BEAN_NAME);
        attribute.put("displayFormat", "rfc1903");
        attribute.put("oid", "1.1.11.0");
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.put("severity", "notice");
        event.put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.testnotif");
        event.put("severity", "panic");
        event.put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.plainnotif");
        event.put("severity", "notice");
        event.put("objectName", BEAN_NAME);
    }
}