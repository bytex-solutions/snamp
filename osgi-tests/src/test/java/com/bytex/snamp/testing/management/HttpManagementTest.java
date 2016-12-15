package com.bytex.snamp.testing.management;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.security.web.Authenticator;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.gson.*;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
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
@SnampDependencies({SnampFeature.SNMP_GATEWAY, SnampFeature.GROOVY_GATEWAY,
SnampFeature.NAGIOS_GATEWAY, SnampFeature.NRDP_GATEWAY, SnampFeature.SSH_GATEWAY, SnampFeature.WEBCONSOLE})
public final class HttpManagementTest extends AbstractJmxConnectorTest<TestOpenMBean> {

    private static final String ADAPTER_INSTANCE_NAME = "test-snmp";
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final String TEST_PARAMETER = "testParameter";

    private static final String COOKIES_HEADER = "Set-Cookie";
    private static final String USERNAME = "karaf";
    private static final String PASSWORD = "karaf";
    private static final String AUTH_COOKIE = WebSecurityFilter.DEFAULT_AUTH_COOKIE;
    private final Authenticator authenticator;

    /**
     * Instantiates a new Snamp webconsole test.
     *
     * @throws MalformedObjectNameException the malformed object name exception
     */
    public HttpManagementTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
        authenticator = new Authenticator();
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    private static HttpCookie authenticate(final String username, final String password) throws IOException, InterruptedException {
        final URL query = new URL("http://localhost:8181/snamp/security/login");
        // we should wait a while before it becomes reachable
        Thread.sleep(5_000);
        //write attribute
        final HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        connection.setRequestProperty("charset", "utf-8");
        connection.setInstanceFollowRedirects(false);
        IOUtils.writeString(String.format("username=%s&password=%s", username, password),
                connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        HttpCookie authCookie = null;
        try {
            if (HttpURLConnection.HTTP_NO_CONTENT != connection.getResponseCode()) {
                throw new IllegalArgumentException(
                        String.format("Wrong response code (%s) received on the authentication phase",
                                connection.getResponseCode()));
            }

            final Map<String, List<String>> headerFields = connection.getHeaderFields();
            final List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
            assertFalse(cookiesHeader.isEmpty());

            for (final String cookie : cookiesHeader) {
                final HttpCookie current = HttpCookie.parse(cookie).get(0);
                if (current.getName().equalsIgnoreCase(AUTH_COOKIE)) {
                    authCookie = current;
                    break;
                }
            }
            assertNotNull(authCookie);
        }
        finally {
            connection.disconnect();
        }
        return authCookie;
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
    public void testLoginValidCredentials() throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);
        assertNotNull(cookie);
        final Principal p = authenticator.parsePrincipal(cookie.getValue());
        assertNotNull(p);
        assertEquals(p.getName(), USERNAME);
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
        authenticate(UUID.randomUUID().toString(), UUID.randomUUID().toString());
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
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);
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
    public void testGetStaticFiles() throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {

        Thread.sleep(2000);
        // Test welcome files (no files are specified manually)
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertTrue(attributeValue.contains("<h3 class=\"masthead-brand\">SNAMP WEB UI</h3>"));

        } finally {
            connection.disconnect();
        }
        // Test some html file
        connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/login.html").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertTrue(attributeValue.contains("<h1>Login to SNAMP UI</h1>"));

        } finally {
            connection.disconnect();
        }
        // Test assets
        connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/js/jquery.js").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertTrue(attributeValue.contains("/*! jQuery v3.0.0 | (c) jQuery Foundation | jquery.org/license */"));

        } finally {
            connection.disconnect();
        }

        // Test file that does not exist
        connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/asdasdasdasd.ext").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

    }

    /**
     * Dummy test.
     *
     * @throws InterruptedException the interrupted exception
     */
    //@Test
    public void dummyTest() throws InterruptedException {
        Thread.sleep(10000000);
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
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);

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
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);
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
            JsonParser parser = new JsonParser();
            JsonObject oldResponseJSON = (JsonObject) parser.parse(responseValue);
            JsonObject newResponseJSON = (JsonObject) parser.parse(newConfiguration);

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
            JsonParser parser = new JsonParser();
            JsonObject oldResponseJSON = (JsonObject) parser.parse(responseValue);
            JsonObject newResponseJSON = (JsonObject) parser.parse(newConfiguration);
            oldResponseJSON.getAsJsonObject("parameters").addProperty("dummyParam", "dummyValue");

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
            JsonParser parser = new JsonParser();
            JsonObject oldResponseJSON = (JsonObject) parser.parse(responseValue);
            JsonObject newResponseJSON = (JsonObject) parser.parse(newConfiguration);
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

        final JsonObject attributeMap = new JsonObject();
        final JsonObject newAttribute1 = new JsonObject();
        final JsonObject newAttribute1Params = new JsonObject();
        newAttribute1Params.add("name", new JsonPrimitive("string"));
        newAttribute1Params.add("objectName", new JsonPrimitive(BEAN_NAME));
        newAttribute1Params.add("oid", new JsonPrimitive("1.1.123.0"));
        newAttribute1.add("parameters", newAttribute1Params);
        newAttribute1.add("readWriteTimeout", null);

        final JsonObject newAttribute2 = new JsonObject();
        final JsonObject newAttribute2Params = new JsonObject();
        newAttribute2Params.add("name", new JsonPrimitive("string"));
        newAttribute2Params.add("objectName", new JsonPrimitive(BEAN_NAME));
        newAttribute2Params.add("oid", new JsonPrimitive("1.1.124.0"));
        newAttribute2.add("parameters", newAttribute2Params);
        newAttribute2.add("readWriteTimeout", null);

        final JsonObject newAttribute3 = new JsonObject();
        final JsonObject newAttribute3Params = new JsonObject();
        newAttribute3Params.add("name", new JsonPrimitive("string"));
        newAttribute3Params.add("objectName", new JsonPrimitive(BEAN_NAME));
        newAttribute3Params.add("oid", new JsonPrimitive("1.1.125.0"));
        newAttribute3.add("parameters", newAttribute3Params);
        newAttribute3.add("readWriteTimeout", null);

        attributeMap.add("123.0", newAttribute1);
        attributeMap.add("124.0", newAttribute2);
        attributeMap.add("125.0", newAttribute3);

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
            JsonParser parser = new JsonParser();
            JsonObject oldResponseJSON = (JsonObject) parser.parse(responseValue);
            JsonObject newResponseJSON = (JsonObject) parser.parse(newConfiguration);
            oldResponseJSON.remove("attributes");
            oldResponseJSON.add("attributes", attributeMap);
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
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);
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
            JsonParser parser = new JsonParser();
            JsonObject oldResponseJSON = (JsonObject) parser.parse(responseValue);
            JsonObject newResponseJSON = (JsonObject) parser.parse(newConfiguration);

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
            JsonParser parser = new JsonParser();
            JsonObject oldResponseJSON = (JsonObject) parser.parse(responseValue);
            oldResponseJSON.getAsJsonObject("parameters").remove(TEST_PARAMETER);
            JsonObject newResponseJSON = (JsonObject) parser.parse(newConfiguration);
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
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);

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
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);

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
            final JsonArray resources = (JsonArray) new JsonParser().parse(responseValue);

            final Optional<JsonElement> element = StreamSupport.stream(resources.spliterator(), false)
                    .filter(entry -> entry.getAsJsonObject().get("name").getAsString().equalsIgnoreCase("JMX Connector"))
                    .findFirst();
            assertTrue(element.isPresent());
            assertEquals("ACTIVE", element.get().getAsJsonObject().get("state").getAsString());

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
            final JsonArray resources = (JsonArray) new JsonParser().parse(responseValue);

            final Optional<JsonElement> element = StreamSupport.stream(resources.spliterator(), false)
                    .filter(entry -> entry.getAsJsonObject().get("name").getAsString().equalsIgnoreCase("JMX Connector"))
                    .findFirst();
            assertTrue(element.isPresent());
            assertEquals("RESOLVED", element.get().getAsJsonObject().get("state").getAsString());

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
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);

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
        syncWithGatewayStartedEvent(ADAPTER_NAME, (BundleExceptionCallable) () -> {
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
        snmpAdapter.getParameters().put("port", SNMP_PORT);
        snmpAdapter.getParameters().put("host", SNMP_HOST);
        snmpAdapter.getParameters().put(TEST_PARAMETER, "parameter");
        snmpAdapter.getParameters().put("socketTimeout", "5000");
        snmpAdapter.getParameters().put("context", "1.1");

        // second instance of gateways for better console default content (dummyTest support)
        final GatewayConfiguration snmpAdapterDummy = gateways.getOrAdd("new_snmp_adapter");
        snmpAdapterDummy.setType(ADAPTER_NAME);
        snmpAdapterDummy.getParameters().put("port", "3232");
        snmpAdapterDummy.getParameters().put("host", SNMP_HOST);
        snmpAdapterDummy.getParameters().put(TEST_PARAMETER, "parameter");
        snmpAdapterDummy.getParameters().put("socketTimeout", "5000");
        snmpAdapterDummy.getParameters().put("context", "1.2");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        attribute.setAlternativeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.1.0");

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.2.0");

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.3.0");

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.4.0");

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.5.1");

        attribute = attributes.getOrAdd("6.1");
        attribute.setAlternativeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.6.1");

        attribute = attributes.getOrAdd("7.1");
        attribute.setAlternativeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.7.1");

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.8.0");

        attribute = attributes.getOrAdd("9.0");
        attribute.setAlternativeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.getParameters().put("oid", "1.1.9.0");

        attribute = attributes.getOrAdd("10.0");
        attribute.setAlternativeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903-human-readable");
        attribute.getParameters().put("oid", "1.1.10.0");

        attribute = attributes.getOrAdd("11.0");
        attribute.setAlternativeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903");
        attribute.getParameters().put("oid", "1.1.11.0");
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);

        event = events.getOrAdd("com.bytex.snamp.connector.tests.impl.plainnotif");
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
    }
}