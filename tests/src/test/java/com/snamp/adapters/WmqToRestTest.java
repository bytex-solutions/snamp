package com.snamp.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.snamp.configuration.EmbeddedAgentConfiguration;
import com.snamp.connectors.IbmWmqConnectorTest;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Черновский Олег Эдуардович
 */
public class WmqToRestTest extends IbmWmqConnectorTest {
    private static final Map<String, String> restAdapterSettings = new HashMap<String, String>(2){{
        put(Adapter.PORT_PARAM_NAME, "3222");
        put(Adapter.ADDRESS_PARAM_NAME, "127.0.0.1");
    }};
    private final Gson jsonFormatter;


    public WmqToRestTest() {
        super("rest", restAdapterSettings);
        jsonFormatter = new Gson();
    }

    private URL buildAttributeURL(final String postfix) throws MalformedURLException {
        return new URL(String.format("http://%s:%s/snamp/management/attribute/%s/%s", restAdapterSettings.get(Adapter.ADDRESS_PARAM_NAME), restAdapterSettings.get(Adapter.PORT_PARAM_NAME), getAttributesNamespace(), postfix));
    }

    private String readAttribute(final String postfix) throws IOException {
        final URL attributeGetter = buildAttributeURL(postfix);
        final HttpURLConnection connection = (HttpURLConnection)attributeGetter.openConnection();
        connection.setRequestMethod("GET");
        assertEquals(MediaType.APPLICATION_JSON, connection.getContentType());
        final StringBuilder result = new StringBuilder();
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        finally {
            connection.disconnect();
        }
        assertEquals(200, connection.getResponseCode());
        return result.toString();
    }

    private  <T> T readAttribute(final String postfix, final Class<T> attributeType) throws IOException {
        return jsonFormatter.fromJson(readAttribute(postfix), attributeType);
    }

    private JsonElement readAttributeAsJson(final String postfix) throws IOException{
        final JsonParser reader = new JsonParser();
        return reader.parse(readAttribute(postfix));
    }

    @Override
    protected void fillAttributes(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> attributes) {
        attributes.put("0", new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("servicesStatus"));
        attributes.put("1", new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("qmgrStatus"));
        attributes.put("2", new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("channelsStatus"));
    }

    @Test
    public final void testForTableProperty() throws IOException {
        JsonElement result = readAttributeAsJson("1");
        assertTrue(result instanceof JsonArray);
        assertTrue(result.getAsJsonArray().get(0).getAsJsonObject().get("MQIACF_Q_MGR_STATUS").getAsString().equals("2")); // QMGR is running
    }
}
