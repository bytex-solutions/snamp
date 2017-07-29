package com.bytex.snamp.testing.gateway.influx;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.Communicator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.bytex.snamp.core.SharedObjectType.COMMUNICATOR;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Path("/")
public class InfluxWriteMock extends InfluxMethodMock {
    private final Communicator communicator;
    private static final String INFLUX_CHANNEL = "InfluxDB-channel";

    InfluxWriteMock(){
        communicator = getCommunicator(ClusterMember.get(null));
    }

    static Communicator getCommunicator(final ClusterMember member){
        return member.getService(INFLUX_CHANNEL, COMMUNICATOR).orElseThrow(AssertionError::new);
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response writePoints(@QueryParam("u") final String username,
                                @QueryParam("p") final String password,
                                @QueryParam("db") final String database,
                                @QueryParam("rp") final String retentionPolicy,
                                @QueryParam("precision") final String precision,
                                @QueryParam("consistency") final String consistency,
                                final String batchPoints){
        communicator.sendSignal(batchPoints);
        return Response.ok().build();
    }
}
