package com.bytex.snamp.testing.connector.http;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.instrumentation.IntegerMeasurement;
import com.bytex.snamp.instrumentation.Measurement;
import com.bytex.snamp.io.IOUtils;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import javax.management.JMException;
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
        return false;
    }

    private void httpPost(final String jsonData, final String url) throws IOException {
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

    private void sendMeasurement(final Measurement measurement) throws IOException{
        httpPost(measurement.toJsonString(), "http://localhost:8181/snamp/data/acquisition");
    }

    private void sendMeasurements(final Measurement... measurements) throws IOException{
        httpPost(Measurement.toJsonString(measurements), "http://localhost:8181/snamp/data/acquisition/batch");
    }

    @Test
    public void testLastValueExtraction2() throws IOException, JMException {
        final IntegerMeasurement measurement1 = new IntegerMeasurement();
        measurement1.setValue(41L);
        measurement1.setInstanceName(INSTANCE_NAME);
        measurement1.setComponentName(COMPONENT_NAME);
        final IntegerMeasurement measurement2 = new IntegerMeasurement();
        measurement2.setValue(46L);
        measurement2.setInstanceName(INSTANCE_NAME);
        measurement2.setComponentName(COMPONENT_NAME);
        sendMeasurements(measurement1, measurement2);
        testAttribute("min", TypeToken.of(Long.class), 41L, true);
        testAttribute("max", TypeToken.of(Long.class), 46L, true);
    }

    @Test
    public void testLastValueExtraction() throws IOException, JMException {
        final IntegerMeasurement measurement = new IntegerMeasurement();
        measurement.setValue(42L);
        measurement.setInstanceName(INSTANCE_NAME);
        measurement.setComponentName(COMPONENT_NAME);
        sendMeasurement(measurement);
        testAttribute("longValue", TypeToken.of(Long.class), 42L, true);
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("attribute1", attribute -> attribute.setAlternativeName("gauge64"));
        attributes.addAndConsume("longValue", attribute -> attribute.setAlternativeName("get lastValue from gauge64 attribute1"));
        attributes.addAndConsume("min", attribute -> attribute.setAlternativeName("get minValue from gauge64 attribute1"));
        attributes.addAndConsume("max", attribute -> attribute.setAlternativeName("get maxValue from gauge64 attribute1"));
    }
}
