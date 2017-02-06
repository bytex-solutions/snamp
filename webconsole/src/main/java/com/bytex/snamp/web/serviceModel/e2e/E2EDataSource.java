package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import com.bytex.snamp.web.serviceModel.ComputingService;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
public final class E2EDataSource extends ComputingService<E2EView, Object, Dashboard> {
    public static final String NAME = "E2E";
    public static final String URL_CONTEXT = "/e2e";

    private final TopologyAnalyzer analyzer;

    public E2EDataSource(final TopologyAnalyzer topologyAnalyzer) throws IOException {
        super(Dashboard.class);
        analyzer = Objects.requireNonNull(topologyAnalyzer);
    }

    @Override
    protected void initialize() {
        //nothing to do
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
        return input.build(analyzer);
    }

    @Nonnull
    @Override
    protected Dashboard createUserData() {
        return new Dashboard();
    }
}
