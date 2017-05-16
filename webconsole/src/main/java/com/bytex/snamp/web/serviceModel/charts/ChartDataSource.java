package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.web.serviceModel.ComputingService;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Represents source of charts data.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class ChartDataSource extends ComputingService<List<Chart>, Map<String, Collection<ChartData>>, Dashboard> implements RESTController {
    private static final String URL_CONTEXT = "/charts";
    private final ExecutorService threadPool;

    public ChartDataSource(@Nonnull final ExecutorService threadPool) {
        super(Dashboard.class);
        this.threadPool = threadPool;
    }

    private static final class ChartDataSeries{
        private final String chartName;
        private final Iterable<? extends ChartData> series;

        private ChartDataSeries(final Chart chart,
                                final BundleContext context) throws Exception {
            chartName = chart.getName();
            series = chart.collectChartData(context);
        }

        private <E extends Exception> ChartDataSeries(final Chart chart,
                                                      final BundleContext context,
                                                      final Function<? super Exception, E> exceptionFactory) throws E {
            chartName = chart.getName();
            series = callAndWrapException(() -> chart.collectChartData(context), exceptionFactory);
        }

        void exportTo(final Multimap<String, ChartData> output){
            output.putAll(chartName, series);
        }
    }

    private static Multimap<String, ChartData> compute(final BundleContext context,
                                                       final Collection<Chart> charts,
                                                       final ExecutorService threadPool) {
        final class ChartCollectionTaskList extends LinkedList<Callable<ChartDataSeries>> implements Callable<Collection<Future<ChartDataSeries>>>, Consumer<Chart> {
            private static final long serialVersionUID = -8887756456174880343L;

            @Override
            public List<Future<ChartDataSeries>> call() throws InterruptedException {
                return threadPool.invokeAll(this, 60, TimeUnit.SECONDS);
            }

            @Override
            public void accept(final Chart chart) {
                add(() -> new ChartDataSeries(chart, context));
            }
        }

        final ChartCollectionTaskList tasks = new ChartCollectionTaskList();
        charts.forEach(tasks);
        return callAndWrapException(tasks, e -> new WebApplicationException(e, Response.status(408).build()))
                .stream()
                .filter(Future::isDone)
                .map(task -> callAndWrapException(task::get, WebApplicationException::new))
                .collect(HashMultimap::<String, ChartData>create, (result, series) -> series.exportTo(result), HashMultimap::putAll);
    }

    private static Multimap<String, ChartData> compute(final BundleContext context, final Collection<Chart> charts) {
        return charts.stream()
                .map(chart -> new ChartDataSeries(chart, context, WebApplicationException::new))
                .collect(HashMultimap::<String, ChartData>create, (result, series) -> series.exportTo(result), HashMultimap::putAll);
    }

    @POST
    @Path("/compute")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Map<String, Collection<ChartData>> compute(final List<Chart> charts) throws WebApplicationException {
        switch (charts.size()) {
            case 0:
                return ImmutableMap.of();
            case 1:
            case 2:
                return compute(getBundleContext(), charts).asMap();
            default:
                return compute(getBundleContext(), charts, threadPool).asMap();
        }
    }

    @Override
    protected void initialize() {
    }

    @Override
    public String getUrlContext() {
        return URL_CONTEXT;
    }

    @Nonnull
    @Override
    protected Dashboard createUserData() {
        return new Dashboard();
    }
}
