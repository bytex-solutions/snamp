package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import com.bytex.snamp.web.serviceModel.ComputingService;
import com.bytex.snamp.web.serviceModel.RESTController;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@Path("/")
public final class E2EDataSource extends ComputingService<E2EView, Object, Dashboard> implements RESTController {
    private static final String URL_CONTEXT = "/e2e";

    public E2EDataSource() {
        super(Dashboard.class);
    }

    @Override
    protected void initialize() {
        //nothing to do
    }

    private static WebApplicationException notAvailable(){
        return new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Topology analyzer is not configured properly").build());
    }

    private <T> Optional<T> processAnalyzer(final Function<? super TopologyAnalyzer, ? extends T> handler) {
        final BundleContext context = getBundleContext();
        final Optional<ServiceHolder<TopologyAnalyzer>> analyzerRef = ServiceHolder.tryCreate(context, TopologyAnalyzer.class);
        if (analyzerRef.isPresent()) {
            final ServiceHolder<TopologyAnalyzer> analyzer = analyzerRef.get();
            final T result;
            try {
                result = handler.apply(analyzer.get());
            } finally {
                analyzer.release(context);
            }
            return Optional.ofNullable(result);
        } else
            return Optional.empty();
    }

    @POST
    @Path("/reset")
    public Response reset() {
        return processAnalyzer(topologyAnalyzer -> {
            topologyAnalyzer.reset();
            return Response.noContent().build();
        }).orElseThrow(E2EDataSource::notAvailable);
    }

    @Override
    public String getUrlContext() {
        return URL_CONTEXT;
    }

    /**
     * Performs idempotent computation.
     *
     * @param input Input argument to process.
     * @return Computation result.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/compute")
    @Override
    public Object compute(final E2EView input) {
        return processAnalyzer(input::build)
                .orElseThrow(E2EDataSource::notAvailable);
    }

    @Nonnull
    @Override
    protected Dashboard createUserData() {
        return new Dashboard();
    }
}
