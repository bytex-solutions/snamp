package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.ws.rs.Path;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents source of charts data.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class ChartDataSource extends AbstractPrincipalBoundedService<Dashboard> {
    public static final String NAME = "charts";
    public static final String URL_CONTEXT = "/charts";

    private final class AttributeSupplierThread extends Repeater {
        private AttributeSupplierThread(final Duration period) {
            super(period);
        }

        private void processAttributes(final WebConsoleSession session, final String resourceName, final AttributeList attributes) throws ReflectionException, MBeanException {
            final Dashboard dashboard = getUserData(session);
            final ChartDataMessage message = new ChartDataMessage(ChartDataSource.this);
            dashboard.getCharts().stream()
                    .filter(chart -> chart instanceof ChartOfAttributeValues)
                    .forEach(chart -> ((ChartOfAttributeValues) chart).fillCharData(resourceName, attributes, message.getChartData()));
            session.sendMessage(message);
        }

        private void doActionImpl(final WebConsoleSession session, final Thread actionThread) {
            for (final Map.Entry<String, ServiceReference<ManagedResourceConnector>> connector : ManagedResourceConnectorClient.getConnectors(getBundleContext()).entrySet()) {
                if (actionThread.isInterrupted())
                    return;   //if submitter is interrupted then exit
                else
                    threadPool.submit(() -> {
                        if (actionThread.isInterrupted())
                            return null;    //if submitter is interrupted then exit
                        /*
                            A reference to managed resource connector used for obtaining attribute values only.
                         */
                        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getBundleContext(), connector.getValue());
                        final AttributeList attributes;
                        final String resourceName = client.getManagedResourceName();
                        try {
                            attributes = client.getAttributes();
                        } finally {
                            client.release(getBundleContext()); //release active reference to the managed resource connector as soon as possible to relax OSGi ServiceRegistry
                        }
                        if (!actionThread.isInterrupted())
                            processAttributes(session, resourceName, attributes);
                        return null;
                    });
            }
        }

        @Override
        protected void doAction() {
            final Thread actionThread = Thread.currentThread();
            forEachSession(session -> doActionImpl(session, actionThread));
        }
    }

    private final ExecutorService threadPool;
    private AttributeSupplierThread attributeSupplier;

    public ChartDataSource(final ConfigurationManager manager, final ExecutorService threadPool) throws IOException {
        super(Dashboard.class);
        this.threadPool = Objects.requireNonNull(threadPool);
        final Duration refreshTime = manager.transformConfiguration(config -> {
            final String RENEW_TIME = "chartDataRefreshTime";
            final long renewTime = MapUtils.getValue(config, RENEW_TIME, Long::parseLong).orElse(900L);
            return Duration.ofMillis(renewTime);
        });
        attributeSupplier = new AttributeSupplierThread(refreshTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        attributeSupplier.run();
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    @Nonnull
    @Override
    protected Dashboard createUserData() {
        return new Dashboard();
    }

    @Override
    public void close() throws Exception {
        super.close();
        try {
            attributeSupplier.close(attributeSupplier.getPeriod());
        } finally {
            attributeSupplier = null;
        }
    }
}
