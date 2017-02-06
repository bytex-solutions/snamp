package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import com.bytex.snamp.web.serviceModel.WebMessage;
import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static com.bytex.snamp.internal.Utils.parallelForEach;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
public final class E2EDataSource extends AbstractPrincipalBoundedService<Dashboard> {
    public static final String NAME = "E2E";
    public static final String URL_CONTEXT = "/e2e";

    private static final class ViewDataCollector extends HashMap<String, Object> implements Consumer<E2EView> {
        private final TopologyAnalyzer analyzer;

        private ViewDataCollector(final TopologyAnalyzer graphAnalyzer, final int expectedSize) {
            super((int) (expectedSize / 0.75F + 1.0F));
            analyzer = graphAnalyzer;
        }

        @Override
        public void accept(final E2EView view) {
            final Object data = view.build(analyzer);
            if (data != null)
                put(view.getName(), data);
        }
    }

    private final TopologyAnalyzer analyzer;

    public E2EDataSource(final TopologyAnalyzer topologyAnalyzer) throws IOException {
        super(Dashboard.class);
        analyzer = Objects.requireNonNull(topologyAnalyzer);
    }

    @Override
    protected void initialize() {
        //nothing to do
    }

    private Map<String, ?> collectViewData(final List<E2EView> views){
        final ViewDataCollector collector = new ViewDataCollector(analyzer, views.size());
        views.forEach(collector);
        return collector;
    }

    /**
     * Collects E2E views.
     * @param dashboard A dashboard with configured views.
     * @return A map with view data.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, ?> collectViewData(final Dashboard dashboard) {
        return collectViewData(dashboard.getViews());
    }

    @Nonnull
    @Override
    protected Dashboard createUserData() {
        return new Dashboard();
    }
}
