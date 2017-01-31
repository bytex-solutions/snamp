package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import com.bytex.snamp.web.serviceModel.WebMessage;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.ws.rs.Path;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.parallelForEach;

/**
 * Represents source of charts data.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class ChartDataSource extends AbstractPrincipalBoundedService<Dashboard> {
    private static final String CHART_DATA_REFRESH_TIME_PARAM = "chartDataRefreshTime";
    public static final String NAME = "charts";
    public static final String URL_CONTEXT = "/charts";

    @JsonTypeName("chartData")
    public static final class ChartDataMessage extends WebMessage {
        private static final long serialVersionUID = 2810215967189225444L;
        private final Map<String, ChartData> chartData;

        private ChartDataMessage(final ChartDataSource source) {
            super(source);
            chartData = new HashMap<>();
        }

        @JsonProperty("dataForCharts")
        public Map<String, ChartData> getChartData(){
            return chartData;
        }
    }

    private final class AttributeSupplierThread extends Repeater {
        private AttributeSupplierThread(final Duration period) {
            super(period);
        }

        private void processAttributes(final WebConsoleSession session, final String resourceName, final AttributeList attributes) {
            final Dashboard dashboard = getUserData(session);
            final ChartDataMessage message = new ChartDataMessage(ChartDataSource.this);
            dashboard.getCharts().stream()
                    .filter(chart -> chart instanceof ChartOfAttributeValues)
                    .forEach(chart -> ((ChartOfAttributeValues) chart).fillCharData(resourceName, attributes, message.getChartData()));
            session.sendMessage(message);
        }

        @Override
        protected String generateThreadName() {
            return getClass().getSimpleName();
        }

        private void processConnector(final Map.Entry<String, ServiceReference<ManagedResourceConnector>> connector,
                                      final Thread actionThread) {
            if (actionThread.isInterrupted()) return;
            final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getBundleContext(), connector.getValue());
            final AttributeList attributes;
            final String resourceName = client.getManagedResourceName();
            try {
                attributes = client.getAttributes();
            } catch (final JMException e) {
                getLogger().log(Level.WARNING, String.format("Unable to read attributes of resource %s", resourceName), e);
                return;
            } finally {
                client.release(getBundleContext()); //release active reference to the managed resource connector as soon as possible to relax OSGi ServiceRegistry
            }
            forEachSession(session -> processAttributes(session, resourceName, attributes));
        }

        @Override
        protected void doAction() {
            final Thread actionThread = Thread.currentThread();
            final Set<Map.Entry<String, ServiceReference<ManagedResourceConnector>>> connectors =
                    ManagedResourceConnectorClient.getConnectors(getBundleContext()).entrySet();
            parallelForEach(connectors, entry -> processConnector(entry, actionThread), threadPool);
        }
    }

    private final ExecutorService threadPool;
    private AttributeSupplierThread attributeSupplier;

    public ChartDataSource(final ConfigurationManager manager, final ExecutorService threadPool) throws IOException {
        super(Dashboard.class);
        this.threadPool = Objects.requireNonNull(threadPool);
        attributeSupplier = new AttributeSupplierThread(getRefreshTime(manager));
    }

    private static Duration getRefreshTime(final ConfigurationManager manager) throws IOException {
        return manager.transformConfiguration(config -> {
            final long renewTime = MapUtils.getValue(config, CHART_DATA_REFRESH_TIME_PARAM, Long::parseLong).orElse(900L);
            return Duration.ofMillis(renewTime);
        });
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForBundle(getBundleContext());
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
