package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class VersionResource extends AbstractWebConsoleService {
    public static final String NAME = "versionInformation";
    public static final String URL_CONTEXT = "/version";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getVersion(){
        return Utils.getBundleContextOfObject(this).getBundle().getVersion().toString();
    }
}