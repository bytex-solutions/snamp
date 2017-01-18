package com.bytex.snamp.testing.gateway.influx;

import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.instrumentation.measurements.IntegerMeasurement;
import com.bytex.snamp.instrumentation.measurements.StandardMeasurements;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.http.AbstractHttpConnectorTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies(SnampFeature.INFLUX_GATEWAY)
public class HttpToInfluxGatewayTest extends AbstractHttpConnectorTest {
    private static final String INSTANCE_NAME = COMPONENT_NAME + "-1";
    private static final String GATEWAY_NAME = "influx";
    private ServiceHolder<HttpService> httpService;

    public HttpToInfluxGatewayTest() {
        super(INSTANCE_NAME);
    }

    private static void ping() throws IOException {
        final URL postAddress = new URL("http://localhost:8181/ping");
        final HttpURLConnection connection = (HttpURLConnection)postAddress.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        try{
            assertEquals(200, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        //setup InfluxDB emulation service
        httpService = ServiceHolder.tryCreate(context, HttpService.class);
        assertNotNull(httpService);
        httpService.get().registerServlet(InfluxPingServlet.CONTEXT, new InfluxPingServlet(), new Hashtable(), null);
        httpService.get().registerServlet(InfluxQueryServlet.CONTEXT, new InfluxQueryServlet(), new Hashtable(), null);
        httpService.get().registerServlet(InfluxWriteServlet.CONTEXT, new InfluxWriteServlet(), new Hashtable(), null);
        ping();
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(GATEWAY_NAME, () -> {
            GatewayActivator.enableGateway(getTestBundleContext(), GATEWAY_NAME);
            return null;
        }, Duration.ofSeconds(15));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        //release InfluxDB emulation service
        assertNotNull(httpService);
        httpService.get().unregister(InfluxPingServlet.CONTEXT);
        httpService.get().unregister(InfluxQueryServlet.CONTEXT);
        httpService.get().unregister(InfluxWriteServlet.CONTEXT);
        httpService.release(context);
        super.afterCleanupTest(context);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void measurementTest() throws IOException, InterruptedException, TimeoutException {
        final IntegerMeasurement measurement = StandardMeasurements.usedRAM(100500L);
        measurement.setComponentName(COMPONENT_NAME);
        measurement.setInstanceName(INSTANCE_NAME);
        sendMeasurement(measurement);
        //now we expect that the notification will be recorded into InfluxDB
        final Communicator communicator = DistributedServices.getProcessLocalCommunicator(InfluxWriteMock.INFLUX_CHANNEL);
        final Serializable points = communicator.receiveMessage(Communicator.ANY_MESSAGE, Communicator.IncomingMessage::getPayload, Duration.ofSeconds(2));
        assertTrue(points instanceof String);
        assertTrue(points.toString().startsWith("usedRAM,connectionString=javaApp-1,connectionType=http,managedResource=test-target value=100500i"));
    }

//    @Test
//    public void realInfluxDBTest() throws IOException, InterruptedException {
//        while (true) {
//            final IntegerMeasurement measurement = StandardMeasurements.usedRAM(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
//            measurement.setComponentName(COMPONENT_NAME);
//            measurement.setInstanceName(INSTANCE_NAME);
//            sendMeasurement(measurement);
//            Thread.sleep(5_000);
//        }
//    }

    @Test
    public void configurationDescriptorTest() throws BundleException {
        ConfigurationEntityDescription desc = GatewayClient.getConfigurationEntityDescriptor(getTestBundleContext(), GATEWAY_NAME, GatewayConfiguration.class);
        testConfigurationDescriptor(desc, "databaseLocation", "databaseLogin", "databasePassword", "databaseName", "uploadPeriod");
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        gateways.addAndConsume("test-gateway", gateway -> {
            gateway.setType(GATEWAY_NAME);
            gateway.put("databaseLocation", "http://localhost:8181/");
            gateway.put("databaseLogin", "dummy");
            gateway.put("databasePassword", "qwerty");
            gateway.put("databaseName", "snamp");
        });
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        events.addAndConsume(StandardMeasurements.USED_RAM, event -> event.setAlternativeName("com.bytex.snamp.measurement.value"));
    }
}
