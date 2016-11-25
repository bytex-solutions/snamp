package com.bytex.snamp.testing.gateway.influx;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class InfluxQueryMock extends InfluxMethodMock {
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postQuery(@QueryParam("u") final String username,
                              @QueryParam("p") final String password,
                              @QueryParam("q") final String query){
        return Response
                .ok()
                .entity("{\"results\":[{\"series\":[{\"name\":\"databases\",\"columns\":[\"name\"],\"values\":[[\"snamp\"]]}]}]}")
                .build();
    }
}
