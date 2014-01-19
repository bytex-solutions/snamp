package com.snamp.adapters;

import com.google.gson.*;
import com.snamp.connectors.IbmWmbConnectorTest;
import com.snamp.hosting.AgentConfiguration;
import com.snamp.hosting.EmbeddedAgentConfiguration;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static com.snamp.hosting.EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс теста IBM WMB-коннектора и его системы типов
 *
 * @author Черновский Олег Эдуардович
 */
public class WmbToRestTest extends IbmWmbConnectorTest {
    private static final Map<String, String> restAdapterSettings = new HashMap<String, String>(2){{
        put(Adapter.PORT_PARAM_NAME, "3222");
        put(Adapter.ADDRESS_PARAM_NAME, "127.0.0.1");
    }};
    private final Gson jsonFormatter;

    public WmbToRestTest() {
        super("rest", restAdapterSettings);
        jsonFormatter = new Gson();
    }

    @Override
    protected void fillAttributes(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> attributes) {
        attributes.put("0", new EmbeddedAttributeConfiguration("name"));
        attributes.put("1", new EmbeddedAttributeConfiguration("runningChildrenNames"));
        attributes.put("2", new EmbeddedAttributeConfiguration("properties"));
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

    @Test
    public final void testForTableProperty() throws IOException {
        JsonElement result = readAttributeAsJson("2");
        assertTrue(result instanceof JsonArray);
        assertTrue(((JsonArray) result).size() == 773); // always 773
    }

    @Test
    public final void testForStringProperty() throws IOException {
        String result = readAttribute("0", String.class);
        assertTrue(result.equals("TEST"));
    }

    @Test
    public final void testForArrayProperty() throws IOException {
        JsonElement result = readAttributeAsJson("1");
        assertTrue(result instanceof JsonArray);
        assertTrue(((JsonArray) result).size() == 1); // always 773
    }
}
