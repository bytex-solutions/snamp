package com.bytex.snamp.web;

import com.bytex.snamp.internal.Utils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/version")
public final class VersionResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getVersion(){
        return Utils.getBundleContextOfObject(this).getBundle().getVersion().toString();
    }
}
