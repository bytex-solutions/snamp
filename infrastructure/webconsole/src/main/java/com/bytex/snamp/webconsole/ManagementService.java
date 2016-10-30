package com.bytex.snamp.webconsole;

import org.osgi.service.cm.ConfigurationAdmin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Objects;

/**
 * Provides API for SNAMP configuration management.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/management")
public final class ManagementService {
    private final ConfigurationAdmin configAdmin;

    ManagementService(final ConfigurationAdmin configAdmin){
        this.configAdmin = Objects.requireNonNull(configAdmin);
    }

    @GET
    public Response seyHello() {
        return Response.ok().entity( "Yes, it works." ).build();
    }
}
