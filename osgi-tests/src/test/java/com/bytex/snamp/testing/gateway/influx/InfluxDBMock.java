package com.bytex.snamp.testing.gateway.influx;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Represents HTTP mock for InfluxDB.
 * <p>
 *     For more information, see org.influxdb.impl.InfluxDBService
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class InfluxDBMock {
    @GET
    @Path("/ping")
    public Response ping(){
        return Response
                .ok()
                .header("X-Influxdb-Version", "1.0.0")
                .build();
    }

    @POST
    @Path("/query")
    public Response postQuery(@QueryParam("u") final String username,
                              @QueryParam("p") final String password,
                              @QueryParam("q") final String query){
        return Response.ok().build();
    }

    @POST
    @Path("/write")
    public Response writePoints(@QueryParam("u") final String username,
                                @QueryParam("p") final String password,
                                @QueryParam("db") final String database,
                                @QueryParam("rp") final String retentionPolicy,
                                @QueryParam("precision") final String precision,
                                @QueryParam("consistency") final String consistency,
                                String batchPoints){
        return Response.ok().build();
    }
}
