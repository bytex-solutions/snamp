package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import com.bytex.snamp.web.serviceModel.WebMessage;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.internal.Utils.parallelForEach;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
public final class E2EDataSource extends AbstractPrincipalBoundedService<Dashboard> {
    private static final String TOPOLOGY_REFRESH_TIME_PARAM = "topologyRefreshTime";
    public static final String NAME = "E2E";
    public static final String URL_CONTEXT = "/e2e";

    @JsonTypeName("E2EViewData")
    public static final class ViewDataMessage extends WebMessage{
        private static final long serialVersionUID = -3059792024115805370L;
        private Object viewData;

        private ViewDataMessage(final E2EDataSource service){
            super(service);
            viewData = new HashMap<>();
        }

        private void setViewData(final E2EView view, final TopologyAnalyzer analyzer){
            viewData = view.build(analyzer);
        }

        @JsonProperty("dataForViews")
        public Object getViewData(){
            return viewData;
        }
    }

    @JsonTypeName("componentArrivals")
    public static final class ComponentArrivalsMessage extends WebMessage{
        private static final long serialVersionUID = -4267371223798230629L;
        private ComponentArrivals arrivals;

        private ComponentArrivalsMessage(final E2EDataSource service){
            super(service);
        }

        private void setData(final ComponentVertex vertex){
            arrivals = new ComponentArrivals(vertex);
        }

        @JsonProperty("data")
        public ComponentArrivals getData(){
            return arrivals;
        }
    }

    private static final class ArrivalsMetricSender extends Repeater{
        private final TopologyAnalyzer topologyAnalyzer;
        private final WeakReference<E2EDataSource> serviceRef;

        private ArrivalsMetricSender(final E2EDataSource service, final Duration period, final TopologyAnalyzer analyzer){
            super(period);
            topologyAnalyzer = Objects.requireNonNull(analyzer);
            assert service != null;
            serviceRef = new WeakReference<>(service);
        }

        private void processVertex(final WebConsoleSession session,
                                   final ComponentVertex vertex){
            final E2EDataSource service = serviceRef.get();
            if (service == null) return;
            final ComponentArrivalsMessage message = new ComponentArrivalsMessage(service);
            message.setData(vertex);
            session.sendMessage(message);
        }

        private void processVertex(final ComponentVertex vertex, final Thread actionThread) {
            if (actionThread.isInterrupted()) return;
            final E2EDataSource service = serviceRef.get();
            if (service == null) return;
            service.forEachSession(session -> processVertex(session, vertex));
        }

        @Override
        protected String generateThreadName() {
            return getClass().getSimpleName();
        }

        @Override
        protected void doAction() {
            final Thread actionThread = Thread.currentThread();
            final E2EDataSource service = serviceRef.get();
            if (service == null) return;
            topologyAnalyzer.parallelForEach(vertex -> processVertex(vertex, actionThread), service.threadPool);
        }
    }

    private static final class TopologyBuilder extends Repeater{
        private final TopologyAnalyzer topologyAnalyzer;
        private final WeakReference<E2EDataSource> serviceRef;

        private TopologyBuilder(final E2EDataSource service, final Duration period, final TopologyAnalyzer analyzer) {
            super(period);
            topologyAnalyzer = Objects.requireNonNull(analyzer);
            assert service != null;
            serviceRef = new WeakReference<>(service);
        }

        private void buildViewData(final WebConsoleSession session, final E2EView view, final Thread actionThread) {
            if (actionThread.isInterrupted()) return;
            final E2EDataSource service = serviceRef.get();
            if (service == null) return;
            final ViewDataMessage message = new ViewDataMessage(service);
            message.setViewData(view, topologyAnalyzer);
            session.sendMessage(message);
        }

        private void doActionForSession(final WebConsoleSession session, final Thread actionThread) {
            if (actionThread.isInterrupted()) return;
            final E2EDataSource service = serviceRef.get();
            if (service == null) return;
            final Dashboard dashboard = service.getUserData(session);
            //building graph for each view may be expensive. Therefore we build graph as separated task
            parallelForEach(dashboard.getViews(), view -> buildViewData(session, view, actionThread), service.threadPool);
        }

        @Override
        protected String generateThreadName() {
            return getClass().getSimpleName();
        }

        @Override
        protected void doAction() {
            final Thread actionThread = Thread.currentThread();
            final E2EDataSource service = serviceRef.get();
            if (service == null) return;
            service.forEachSession(session -> doActionForSession(session, actionThread), service.threadPool);
        }
    }

    private final ExecutorService threadPool;
    private TopologyBuilder topologyBuilder;
    private ArrivalsMetricSender arrivalsSender;

    public E2EDataSource(final ConfigurationManager manager,
                         final TopologyAnalyzer topologyAnalyzer,
                         final ExecutorService threadPool) throws IOException {
        super(Dashboard.class);
        this.threadPool = Objects.requireNonNull(threadPool);
        final Duration refreshTime = getTopologyRefreshTime(manager);
        topologyBuilder = new TopologyBuilder(this, refreshTime, topologyAnalyzer);
        arrivalsSender = new ArrivalsMetricSender(this, refreshTime, topologyAnalyzer);
    }

    private static Duration getTopologyRefreshTime(final ConfigurationManager manager) throws IOException {
        return manager.transformConfiguration(config -> {
            final long renewTime = MapUtils.getValue(config, TOPOLOGY_REFRESH_TIME_PARAM, Long::parseLong).orElse(900L);
            return Duration.ofMillis(renewTime);
        });
    }

    /**
     * Initializes this service.
     * <p/>
     * Services for SNAMP Web Console has lazy initialization. They will be initialized when the first session of the client
     * will be attached. This approach helps to save computation resources when SNAMP deployed as cluster with many nodes.
     */
    @Override
    protected void initialize() {
        topologyBuilder.run();
        arrivalsSender.run();
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
            topologyBuilder.close();
            arrivalsSender.close();
        } finally {
            topologyBuilder.serviceRef.clear();
            arrivalsSender.serviceRef.clear();
            topologyBuilder = null;
            arrivalsSender = null;
        }
    }
}
