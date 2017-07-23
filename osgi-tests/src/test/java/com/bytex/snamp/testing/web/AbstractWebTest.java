package com.bytex.snamp.testing.web;

import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractWebTest extends AbstractSnampIntegrationTest {
    static final String DUMMY_TEST_PROPERTY = "com.bytex.snamp.testing.webconsole.dummy.test";

    static final ObjectMapper FORMATTER = new ObjectMapper();

    static boolean isDummyTestEnabled(){
        return Boolean.getBoolean(DUMMY_TEST_PROPERTY);
    }

    static JsonNode httpPost(final String servicePostfix, final String authenticationToken, final JsonNode data) throws IOException {
        final URL attributeQuery = new URL("http://localhost:8181/snamp/web/api" + servicePostfix);
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) attributeQuery.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
        connection.setRequestProperty(HttpHeaders.AUTHORIZATION, authenticationToken);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        IOUtils.writeString(FORMATTER.writeValueAsString(data), connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try {
            assertEquals(200, connection.getResponseCode());
            return FORMATTER.readTree(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }

    static void httpPut(final String servicePrefix, final String servicePostfix, final String authenticationToken, final JsonNode data) throws IOException {
        final URL attributeQuery = new URL(servicePrefix + servicePostfix);
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) attributeQuery.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
        connection.setRequestProperty(HttpHeaders.AUTHORIZATION, authenticationToken);
        connection.setDoOutput(true);
        IOUtils.writeString(FORMATTER.writeValueAsString(data), connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try {
            assertEquals(204, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    static void httpPut(final String servicePostfix, final String authenticationToken, final JsonNode data) throws IOException {
        httpPut("http://localhost:8181/snamp/web/api", servicePostfix, authenticationToken, data);
    }

    static JsonNode httpGet(final String servicePostfix, final String authenticationToken) throws IOException {
        return httpGet("http://localhost:8181/snamp/web/api", servicePostfix, authenticationToken);
    }

    static JsonNode httpGet(final String servicePrefix, final String servicePostfix, final String authenticationToken) throws IOException{
        final URL attributeQuery = new URL(servicePrefix + servicePostfix);
        //write attribute
        HttpURLConnection connection = (HttpURLConnection) attributeQuery.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty(HttpHeaders.AUTHORIZATION, authenticationToken);
        connection.setDoInput(true);
        connection.connect();
        try(final Reader reader = new InputStreamReader(connection.getInputStream(), IOUtils.DEFAULT_CHARSET)){
            assertEquals(200, connection.getResponseCode());
            return FORMATTER.readTree(reader);
        } finally {
            connection.disconnect();
        }
    }

    final TestAuthenticator createAuthenticator(){
        return new TestAuthenticator(getTestBundleContext());
    }
}
