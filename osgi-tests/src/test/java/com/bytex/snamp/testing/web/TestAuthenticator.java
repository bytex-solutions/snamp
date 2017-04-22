package com.bytex.snamp.testing.web;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.security.web.JWTAuthenticator;
import com.bytex.snamp.security.web.WebSecurityFilter;
import org.osgi.framework.BundleContext;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class TestAuthenticator extends JWTAuthenticator {
    private static final String COOKIES_HEADER = "Set-Cookie";
    private static final String TEST_USER_NAME = "karaf";
    private static final String TEST_PASSWORD = "karaf";
    private static final String AUTH_COOKIE = WebSecurityFilter.DEFAULT_AUTH_COOKIE;

    public TestAuthenticator(final BundleContext testContext) {
        super(ClusterMember.get(testContext));
    }

    public HttpCookie authenticateTestUser() throws IOException {
        return authenticateClient(TEST_USER_NAME, TEST_PASSWORD);
    }

    public void verifyTestUser(final String jwToken) throws GeneralSecurityException, IOException {
        verify(TEST_USER_NAME, jwToken);
    }

    public HttpCookie authenticateClient(final String username, final String password) throws IOException {
        final URL query = new URL("http://localhost:8181/snamp/security/login");
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
            assert !cookiesHeader.isEmpty();

            for (final String cookie : cookiesHeader) {
                final HttpCookie current = HttpCookie.parse(cookie).get(0);
                if (current.getName().equalsIgnoreCase(AUTH_COOKIE)) {
                    authCookie = current;
                    break;
                }
            }
            assert authCookie != null;
        }
        finally {
            connection.disconnect();
        }
        return authCookie;
    }
}
