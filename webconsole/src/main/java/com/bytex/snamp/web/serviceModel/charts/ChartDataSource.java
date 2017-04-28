package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.web.serviceModel.ComputingService;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.google.common.collect.*;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.*;

/**
 * Represents source of charts data.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class ChartDataSource extends ComputingService<Chart[], Map<String, Collection<ChartData>>, Dashboard> implements RESTController {
    private static final String URL_CONTEXT = "/charts";
    private final ExecutorService threadPool;

    public ChartDataSource(@Nonnull final ExecutorService threadPool) {
        super(Dashboard.class);
        this.threadPool = threadPool;
    }

    private static final class ChartDataSeries{
        private final String chartName;
        private final Iterable<? extends ChartData> series;

        private ChartDataSeries(final Chart chart, final BundleContext context) throws Exception {
            chartName = chart.getName();
            series = chart.collectChartData(context);
        }

        void exportTo(final Multimap<String, ChartData> output){
            output.putAll(chartName, series);
        }
    }

    private static Callable<ChartDataSeries> createTask(final Chart chart, final BundleContext context) {
        return () -> new ChartDataSeries(chart, context);
    }

    private Multimap<String, ChartData> compute(final Collection<Callable<ChartDataSeries>> tasks) {
        final Collection<Future<ChartDataSeries>> completedTasks;
        try {
            completedTasks = threadPool.invokeAll(tasks, 60, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            throw new WebApplicationException(e, Response.status(408).build());    //too many tasks
        }
        final Multimap<String, ChartData> result = HashMultimap.create(tasks.size(), 4);
        for (final Future<ChartDataSeries> task : completedTasks) {
            assert task.isDone();
            final ChartDataSeries series;
            try {
                series = task.get();
            } catch (final ExecutionException | InterruptedException e) {
                throw new WebApplicationException(e);
            }
            series.exportTo(result);
        }
        return result;
    }

    private static Multimap<String, ChartData> compute(final BundleContext context, final Chart chart) {
        try {
            return ImmutableMultimap.<String, ChartData>builder()
                    .putAll(chart.getName(), chart.collectChartData(context))
                    .build();
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private static Multimap<String, ChartData> compute(final BundleContext context, final Chart first, final Chart second) {
        try {
            return ImmutableMultimap.<String, ChartData>builder()
                    .putAll(first.getName(), first.collectChartData(context))
                    .putAll(second.getName(), second.collectChartData(context))
                    .build();
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/compute")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Map<String, Collection<ChartData>> compute(final Chart[] charts) throws WebApplicationException {
        final BundleContext context = getBundleContext();
        switch (charts.length) {
            case 0:
                return ImmutableMap.of();
            case 1:
                return compute(context, charts[0]).asMap();
            case 2:
                return compute(context, charts[0], charts[1]).asMap();
            default:
                final List<Callable<ChartDataSeries>> tasks = new LinkedList<>();
                Arrays.stream(charts).map(chart -> createTask(chart, context)).forEach(tasks::add);
                return compute(tasks).asMap();
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
