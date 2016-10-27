package com.bytex.snamp.webconsole.data.api;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @param
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path( "/get-data" )
public class ExampleService {

    @GET
    public Response  seyHello() {
        return Response.ok().entity( "Yes, it works." ).build();
    }
}
