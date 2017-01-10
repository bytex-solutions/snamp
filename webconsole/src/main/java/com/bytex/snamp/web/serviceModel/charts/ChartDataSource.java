package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.ReflectionException;
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
public final class ChartDataSource extends AbstractPrincipalBoundedService<Dashboard> {
    private static final String RENEW_TIME = "com.bytex.snamp.web.console.chartDataSource.dataRefreshTime";

    private final class AttributeSupplierThread extends Repeater {
        private AttributeSupplierThread(final Duration period) {
            super(period);
        }

        private void processAttributes(final WebConsoleSession session, final String resourceName, final AttributeList attributes) throws ReflectionException, MBeanException {
            final Dashboard dashboard = getUserData(session);
            final ChartDataMessage message = new ChartDataMessage(ChartDataSource.this);
            for (final Chart chart : dashboard.getCharts())
                if (chart instanceof ChartOfAttributeValues)
                    ((ChartOfAttributeValues) chart).fillCharData(resourceName, attributes, message.getChartData());
        }

        private void doActionImpl(final WebConsoleSession session, final Thread actionThread) {
            for (final Map.Entry<String, ServiceReference<ManagedResourceConnector>> connector : ManagedResourceConnectorClient.getConnectors(getBundleContext()).entrySet()) {
                if (actionThread.isInterrupted())
                    return;   //if submitter is interrupted then exit
                else
                    threadPool.submit(() -> {
                        if (actionThread.isInterrupted())
                            return null;    //if submitter is interrupted then exit
                        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getBundleContext(), connector.getValue());
                        final AttributeSupport attributes = client.queryObject(AttributeSupport.class);
                        if (attributes != null)
                            try {
                                processAttributes(session, client.getManagedResourceName(), attributes.getAttributes());
                            } finally {
                                client.release(getBundleContext());
                            }
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

    public ChartDataSource(final ExecutorService threadPool) {
        super(Dashboard.class);
        this.threadPool = Objects.requireNonNull(threadPool);
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
