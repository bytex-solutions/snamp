package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.management.MBeanAttributeInfo;

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

        private void processAttributes(final AttributeSupport attributes) throws Exception {
            for(final MBeanAttributeInfo attributeInfo: attributes.getAttributeInfo()){

            }
        }

        @Override
        protected void doAction() throws Exception {
            for (final Map.Entry<String, ServiceReference<ManagedResourceConnector>> connector : ManagedResourceConnectorClient.getConnectors(getBundleContext()).entrySet()) {
                final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getBundleContext(), connector.getValue());
                if (!client.isValid()) return;
                try {
                    final AttributeSupport attributes = client.queryObject(AttributeSupport.class);
                    if (attributes != null)
                        processAttributes(attributes);
                } finally {
                    client.release(getBundleContext());
                }
            }
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
