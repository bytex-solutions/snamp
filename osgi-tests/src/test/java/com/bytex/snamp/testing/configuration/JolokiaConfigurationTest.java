package com.bytex.snamp.testing.configuration;

import com.google.gson.*;
import com.bytex.snamp.SafeConsumer;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.SystemProperties;
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
        JsonElement config =
                readAttribute("com.bytex.snamp.management:type=SnampCore", "configuration");
        assertTrue(config.isJsonObject());
        assertEquals(JsonNull.INSTANCE, config.getAsJsonObject().get("value"));
        //change config
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                final ManagedResourceConfiguration resource =
                        config.newConfigurationEntity(ManagedResourceConfiguration.class);
                resource.getParameters().put("param1", "value");
                resource.setConnectionType("snmp");
                resource.setConnectionString("udp://127.0.0.1/161");
                config.getManagedResources().put("res1", resource);
            }
        }, true);
        config = readAttribute("com.bytex.snamp.management:type=SnampCore", "configuration");
        assertTrue(config.isJsonObject());
        assertNotEquals(JsonNull.INSTANCE, config.getAsJsonObject().get("value"));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
    }
}