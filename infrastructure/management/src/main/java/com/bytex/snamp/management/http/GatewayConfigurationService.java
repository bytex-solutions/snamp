package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.management.http.model.FeatureBindingDataObject;
import com.bytex.snamp.management.http.model.GatewayDataObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * Provides API for SNAMP gateway.
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/gateway")
public final class GatewayConfigurationService extends AbstractEntityConfigurationService<GatewayConfiguration, GatewayDataObject> {

    /**
     * Instantiates a new Gateway configuration service.
     */
    public GatewayConfigurationService(){
        super(GatewayConfiguration.class);
    }

    @Override
    protected GatewayDataObject toDataTransferObject(final GatewayConfiguration entity) {
        return new GatewayDataObject(entity);
    }

    /**
     * Gets attributes bindings.
     *
     * @param name the name
     * @param type the type
     * @return the attributes bindings
     */
    @GET
    @Path("/{name}/{feature}/bindings")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, FeatureBindingDataObject>> getBindings(@PathParam("name") final String name,
                                                             @PathParam("feature") final FeatureType type) {
        return type.getBindings(getBundleContext(), name);
    }
}
