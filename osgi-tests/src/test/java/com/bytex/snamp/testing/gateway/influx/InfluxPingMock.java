package com.bytex.snamp.testing.gateway.influx;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
public final class InfluxPingMock extends InfluxMethodMock {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping(){
        return Response
                .ok()
                .header("X-Influxdb-Version", "1.0.0")
                .build();
    }
}
