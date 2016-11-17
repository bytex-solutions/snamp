package com.bytex.snamp.testing.management;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.PropagateSystemProperties;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * The Snamp webconsole test.
 * @author Evgeniy Kirichenko.
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.WEBCONSOLE, SnampFeature.WRAPPED_LIBS})
public final class SnampWebconsoleTest extends AbstractSnampIntegrationTest {

    private static final String COOKIES_HEADER = "Set-Cookie";
    private static final String USERNAME = "karaf";
    private static final String PASSWORD = "karaf";
    private static final String AUTH_COOKIE = "snamp-auth-token";
    private static final String JWT_SECRET_BOX_NAME = "JWT_SECRET";
    private static final String JWT_SECRET = UUID.randomUUID().toString();

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
        Thread.sleep(2000);
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

    @Override
    protected void setupTestConfiguration(AgentConfiguration config) {
        DistributedServices.getDistributedBox(Utils.getBundleContextOfObject(this), JWT_SECRET_BOX_NAME)
                .set(JWT_SECRET);
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

    @Test
    public void dummyTest() throws InterruptedException {
        Thread.sleep(10000000);
    }
}