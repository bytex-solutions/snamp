package com.bytex.snamp.testing.gateway.influx;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.http.AbstractHttpConnectorTest;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import java.time.Duration;
import java.util.Hashtable;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies(SnampFeature.INFLUX_GATEWAY)
public class HttpToInfluxGatewayTest extends AbstractHttpConnectorTest {
    private static final String INFLUX_DB_CONTEXT = "/influx-db";
    private static final String INSTANCE_NAME = "javaApp#1";
    private static final String GATEWAY_INSTANCE = "test-gateway";

    public HttpToInfluxGatewayTest() {
        super(INSTANCE_NAME);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        //setup InfluxDB emulation service
        final ServiceHolder<HttpService> httpService = ServiceHolder.tryCreate(context, HttpService.class);
        assertNotNull(httpService);
        try{
            httpService.get().registerServlet(INFLUX_DB_CONTEXT, new InfluxServlet(), new Hashtable(), null);
        } finally {
            httpService.release(context);
        }
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithGatewayStartedEvent(GATEWAY_INSTANCE, (BundleExceptionCallable)() -> {
            GatewayActivator.enableGateway(getTestBundleContext(), GATEWAY_INSTANCE);
            return null;
        }, Duration.ofSeconds(15));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        GatewayActivator.disableGateway(context, GATEWAY_INSTANCE);
        stopResourceConnector(context);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        //release InfluxDB emulation service
        final ServiceHolder<HttpService> httpService = ServiceHolder.tryCreate(context, HttpService.class);
        assertNotNull(httpService);
        try{
            httpService.get().unregister(INFLUX_DB_CONTEXT);
        } finally {
            httpService.release(context);
        }
        super.afterCleanupTest(context);
    }

    @Override
    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways) {
        gateways.addAndConsume(GATEWAY_INSTANCE, gateway -> {
            gateway.setType("influx");
            gateway.getParameters().put("databaseLocation", "http://127.0.0.1:8181" + INFLUX_DB_CONTEXT);
            gateway.getParameters().put("databaseLogin", "dummy");
            gateway.getParameters().put("databasePassword", "qwerty");
            gateway.getParameters().put("databaseName", "snamp");
        });
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        events.addAndConsume("cpuLoad", event -> {
            event.setAlternativeName("com.bytex.snamp.measurement.value");
        });
    }
}
