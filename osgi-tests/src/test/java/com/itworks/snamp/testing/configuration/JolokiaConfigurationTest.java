package com.itworks.snamp.testing.configuration;

import com.google.gson.*;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.io.IOUtils;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.SystemProperties;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import static com.itworks.snamp.jmx.json.JsonUtils.toJsonObject;
import static com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest.JMX_LOGIN;
import static com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest.JMX_PASSWORD;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.WRAPPED_LIBS)
@SystemProperties({
        "org.jolokia.agentContext=/jolokia",
        "org.jolokia.realm=karaf",
        "org.jolokia.user=" + JMX_LOGIN,
        "org.jolokia.authMode=jaas"
})
public final class JolokiaConfigurationTest extends AbstractSnampIntegrationTest {
    private final Gson formatter = new Gson();

    private static final class JolokiaAuthenticator extends Authenticator{
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(JMX_LOGIN, JMX_PASSWORD.toCharArray());
        }
    }

    private JsonElement readAttribute(final String objectName,
                                             final String attributeName) throws IOException {
        final URL jolokiaQuery = new URL("http://localhost:8181/jolokia");
        //write attribute
        HttpURLConnection connection = (HttpURLConnection)jolokiaQuery.openConnection();
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
    public void processConfigViaJolokia() throws IOException {
        final JsonElement config =
                readAttribute("com.itworks.snamp.management:type=SnampCore", "configuration");
        assertTrue(config.isJsonObject());
        assertNotEquals(JsonNull.INSTANCE, config.getAsJsonObject().get("value"));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
    }
}