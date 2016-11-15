package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.instrumentation.IntegerMeasurement;
import com.bytex.snamp.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class HttpConnectorTest extends AbstractHttpConnectorTest {

    private static final String INSTANCE_NAME = "testInstance";

    public HttpConnectorTest() {
        super(INSTANCE_NAME);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    @Test
    public void testAttribute() throws IOException {
        final URL postAddress = new URL("http://localhost:8181/snamp/data/acquisition");
        final HttpURLConnection connection = (HttpURLConnection)postAddress.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        final IntegerMeasurement measurement = new IntegerMeasurement();
        measurement.setValue(42L);
        measurement.setInstanceName(INSTANCE_NAME);
        measurement.setComponentName(COMPONENT_NAME);
        IOUtils.writeString(measurement.toJsonString(), connection.getOutputStream(), Charset.defaultCharset());
        connection.connect();
        try{
            assertEquals(204, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("attribute1", attribute -> attribute.setAlternativeName("gauge"));
    }
}
