package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies(SnampFeature.HTTP_ACCEPTOR)
public abstract class AbstractHttpConnectorTest extends AbstractResourceConnectorTest {
    public static final String CONNECTOR_TYPE = "http";
    protected static final String COMPONENT_NAME = "javaApp";

    protected AbstractHttpConnectorTest(final String instanceName){
        super(CONNECTOR_TYPE, instanceName, ImmutableMap.of("componentName", COMPONENT_NAME));
    }

    protected AbstractHttpConnectorTest(final String instanceName, final String scriptPath, final String scriptName){
        super(CONNECTOR_TYPE, instanceName, ImmutableMap.of(
                "componentName", COMPONENT_NAME,
                "parserScript", scriptName,
                "parserScriptPath", scriptPath
                )
        );
    }

    private static void httpPost(final String data, final String url, final MediaType type) throws IOException {
        final URL postAddress = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection)postAddress.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", type.toString());
        connection.setDoOutput(true);
        IOUtils.writeString(data, connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try{
            assertEquals(204, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    public static void sendMeasurement(final Measurement measurement) throws IOException{
        httpPost(measurement.toJsonString(), "http://localhost:8181/snamp/data/acquisition/measurement/", MediaType.APPLICATION_JSON_TYPE);
    }

    protected static void sendMeasurements(final Measurement... measurements) throws IOException{
        httpPost(Measurement.toJsonString(measurements), "http://localhost:8181/snamp/data/acquisition/measurements/", MediaType.APPLICATION_JSON_TYPE);
    }

    protected static void sendText(final String text) throws IOException{
        httpPost(text, "http://localhost:8181/snamp/data/acquisition", MediaType.TEXT_PLAIN_TYPE);
    }

    protected static void sendJson(final String json) throws IOException{
        httpPost(json, "http://localhost:8181/snamp/data/acquisition", MediaType.APPLICATION_JSON_TYPE);
    }

    protected static void sendXml(final String xml) throws IOException{
        httpPost(xml, "http://localhost:8181/snamp/data/acquisition", MediaType.TEXT_XML_TYPE);
    }
}
