package com.bytex.snamp.testing.management;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bytex.snamp.testing.connector.jmx.TestOpenMBean.BEAN_NAME;


/**
 * The Snamp webconsole test.
 * @author Evgeniy Kirichenko.
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.WEBCONSOLE, SnampFeature.WRAPPED_LIBS, SnampFeature.SNMP_GATEWAY})
public final class SnampWebconsoleTest extends AbstractJmxConnectorTest<TestOpenMBean> {

    private static final String ADAPTER_INSTANCE_NAME = "test-snmp";
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";

    private static final String COOKIES_HEADER = "Set-Cookie";
    private static final String USERNAME = "karaf";
    private static final String PASSWORD = "karaf";
    private static final String AUTH_COOKIE = "snamp-auth-token";
    private static final String JWT_SECRET_BOX_NAME = "JWT_SECRET";
    private static final String JWT_SECRET = UUID.randomUUID().toString();

    /**
     * Instantiates a new Snamp webconsole test.
     *
     * @throws MalformedObjectNameException the malformed object name exception
     */
    public SnampWebconsoleTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
        DistributedServices.getDistributedBox(Utils.getBundleContextOfObject(this), JWT_SECRET_BOX_NAME)
                .set(JWT_SECRET);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }


    // Available urls:
    // /snamp/console/auth
    // /snamp/console/check
    // /snamp/console//management

    private HttpCookie authenticate(final String username, final String password) throws IOException, InterruptedException {
        final URL query = new URL("http://localhost:8181/snamp/console/auth");
        // we should wait a while before it becomes reachable
        Thread.sleep(5000);
        //write attribute
        final HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
     * @throws JWTVerifyException       the jwt verify exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testLoginValidCredentials() throws IOException, InterruptedException, NoSuchAlgorithmException, JWTVerifyException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);
        assertNotNull(cookie);
        final JWTVerifier jwtVerifier = new JWTVerifier(JWT_SECRET);
        final Map<String, Object> claims = jwtVerifier.verify(cookie.getValue());
        assertFalse(claims.isEmpty());
        assertTrue(claims.containsKey("sub"));
        assertEquals(claims.get("sub"), USERNAME);
    }

    /**
     * Login with invalid credentials.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws JWTVerifyException       the jwt verify exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testLoginInvalidCredentials() throws IOException, InterruptedException, NoSuchAlgorithmException, JWTVerifyException,
            InvalidKeyException, SignatureException {
        boolean failed = false;
        try {
            authenticate(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        } catch (final Throwable e) {
            assertTrue(e instanceof IllegalArgumentException);
            failed = true;
        }
        assertTrue("Authentication with invalid credentials has been done successfully", failed);
    }

    /**
     * Test check simple resource with and without token.
     *
     * @throws IOException              the io exception
     * @throws InterruptedException     the interrupted exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws JWTVerifyException       the jwt verify exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testCheckSimpleResourceWithAndWithoutToken() throws IOException, InterruptedException, NoSuchAlgorithmException, JWTVerifyException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);
        final URL query = new URL("http://localhost:8181/snamp/console/check");
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();
        try {
            assertEquals(String.format("Wrong response code (%s) received on the authentication phase",
                    connection.getResponseCode()), HttpURLConnection.HTTP_NO_CONTENT, connection.getResponseCode());
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
     * @throws JWTVerifyException       the jwt verify exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testGetStaticFiles() throws IOException, InterruptedException, NoSuchAlgorithmException, JWTVerifyException,
            InvalidKeyException, SignatureException {

        Thread.sleep(2000);
        // Test welcome files (no files are specified manually)
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(connection.getResponseCode(), HttpURLConnection.HTTP_OK);
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertTrue(attributeValue.contains("<h1 class=\"cover-heading\">Set up your SNAMP.</h1>"));

        } finally {
            connection.disconnect();
        }
        // Test some html file
        connection = (HttpURLConnection) new URL("http://localhost:8181/snamp/login.html").openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        try {
            assertEquals(connection.getResponseCode(), HttpURLConnection.HTTP_OK);
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
            assertEquals(connection.getResponseCode(), HttpURLConnection.HTTP_OK);
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
            assertEquals(connection.getResponseCode(), HttpURLConnection.HTTP_NOT_FOUND);
        } finally {
            connection.disconnect();
        }

    }

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
     * @throws JWTVerifyException       the jwt verify exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testGetResourceConfiguration() throws IOException, InterruptedException, NoSuchAlgorithmException, JWTVerifyException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);

        // Get all resources
        URL query = new URL("http://localhost:8181/snamp/console/resource");
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
         query = new URL(String.format("http://localhost:8181/snamp/console/resource/%s", TEST_RESOURCE_NAME));
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
     * @throws JWTVerifyException       the jwt verify exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    @Test
    public void testModifyResourceConfiguration() throws IOException, InterruptedException, NoSuchAlgorithmException, JWTVerifyException,
            InvalidKeyException, SignatureException {
        final HttpCookie cookie = authenticate(USERNAME, PASSWORD);
        // Get resource by name
        URL query = new URL(String.format("http://localhost:8181/snamp/console/resource/%s", TEST_RESOURCE_NAME));
        HttpURLConnection connection = (HttpURLConnection) query.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", String.format("Bearer %s", cookie.getValue()));
        connection.connect();

        String responseValue = "";
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
        snmpAdapter.getParameters().put("socketTimeout", "5000");
        snmpAdapter.getParameters().put("context", "1.1");
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
}