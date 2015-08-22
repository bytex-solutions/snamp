package com.bytex.snamp.testing.connectors.mda;

import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.MDA_CONNECTOR)
public abstract class AbstractMdaConnectorTest extends AbstractResourceConnectorTest {
    protected static final String CONNECTOR_TYPE = "mda";
    private final Gson formatter;

    protected AbstractMdaConnectorTest(final Map<String, String> parameters){
        super(CONNECTOR_TYPE, "", parameters);
        formatter = new Gson();
    }

    private URL createURL(final String postfix) throws MalformedURLException {
        return new URL(String.format("http://localhost:8181/snamp/connectors/mda/%s/%s", TEST_RESOURCE_NAME, postfix));
    }

    protected final JsonElement getAttributeViaHttp(final String attributeName) throws IOException{
        final URL requestUrl = createURL("attributes/" + attributeName);
        final HttpURLConnection connection = (HttpURLConnection)requestUrl.openConnection();
        connection.setRequestMethod("GET");
        try(final InputStreamReader reader = new InputStreamReader(connection.getInputStream(), IOUtils.DEFAULT_CHARSET)){

            return formatter.fromJson(reader, JsonElement.class);
        }
        finally {
            connection.disconnect();
        }
    }

    protected final JsonElement setAttributeViaHttp(final String attributeName, final JsonElement value) throws IOException{
        final URL requestUrl = createURL("attributes/" + attributeName);
        final HttpURLConnection connection = (HttpURLConnection)requestUrl.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        try(final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), IOUtils.DEFAULT_CHARSET)) {
            formatter.toJson(value, writer);
        }

        try(final InputStreamReader reader = new InputStreamReader(connection.getInputStream(), IOUtils.DEFAULT_CHARSET)){
            return formatter.fromJson(reader, JsonElement.class);
        }
        finally {
            connection.disconnect();
        }
    }
}
