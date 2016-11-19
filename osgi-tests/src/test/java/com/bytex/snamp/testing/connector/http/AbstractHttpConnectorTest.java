package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.instrumentation.Measurement;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;

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

    private static void httpPost(final String jsonData, final String url) throws IOException {
        final URL postAddress = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection)postAddress.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        IOUtils.writeString(jsonData, connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try{
            assertEquals(204, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    protected static void sendMeasurement(final Measurement measurement) throws IOException{
        httpPost(measurement.toJsonString(), "http://localhost:8181/snamp/data/acquisition");
    }

    protected static void sendMeasurements(final Measurement... measurements) throws IOException{
        httpPost(Measurement.toJsonString(measurements), "http://localhost:8181/snamp/data/acquisition/batch");
    }
}
