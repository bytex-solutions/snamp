package com.bytex.snamp.webconsole;

import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.web.Authenticator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Provides API for SNAMP Web Console.
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class WebConsoleRestService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/version")
    public String getVersion(){
        return Utils.getBundleContextOfObject(this).getBundle().getVersion().toString();
    }
}
