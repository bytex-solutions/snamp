package com.bytex.snamp.webconsole;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.webconsole.model.dto.DTOFactory;
import com.bytex.snamp.webconsole.model.dto.GatewayConfigurationDTO;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Provides API for SNAMP gateway.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/gateway")
public final class GatewayService extends BaseRestConfigurationService {

    /**
     * Returns all the configured gateways.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map getConfiguration() {
        return DTOFactory.buildGateways(readOnlyActions(currentConfig ->
                (EntityMap<? extends GatewayConfiguration>)
                        currentConfig.getEntities(GatewayConfiguration.class)));
    }

    /**
     * Returns configuration for certain configured resource by its name.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public GatewayConfigurationDTO getConfigurationByName(@PathParam("name") final String name) {
        return DTOFactory.buildGateway(readOnlyActions(configuration -> {
            if (configuration.getEntities(GatewayConfiguration.class).get(name) != null) {
                return configuration.getEntities(GatewayConfiguration.class).get(name);
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        }));
    }

    /**
     * Updated certain resource.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @PUT
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setConfigurationByName(@PathParam("name") final String name,
                                           final GatewayConfigurationDTO object) {
        return changingActions(currentConfig -> {
            final EntityMap<? extends GatewayConfiguration> entityMap =
                    currentConfig.getEntities(GatewayConfiguration.class);
            final GatewayConfiguration mrc = entityMap.getOrAdd(name);
            if (mrc != null) {
                mrc.setParameters(object.getParameters());
                mrc.setType(object.getType());
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Remove gateway from configuration by its name
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @DELETE
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeConfigurationByName(@PathParam("name") final String name) {
        return changingActions(currentConfig -> {
            if (currentConfig.getEntities(GatewayConfiguration.class).get(name) == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            } else {
                return currentConfig.getEntities(GatewayConfiguration.class).remove(name) != null;
            }
        });
    }

    /**
     * Returns parameters for certain configured resource by its name.
     *
     * @return Map that contains parameters configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map getParametersForResource(@PathParam("name") final String name) {
        return readOnlyActions(currentConfig -> {
            final GatewayConfiguration mrc =
                    currentConfig.getEntities(GatewayConfiguration.class).get(name);
            if (mrc != null) {
                return mrc.getParameters();
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Set parameters for certain configured resource by its name.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setParametersForResource(@PathParam("name") final String name, final Map<String, String> object) {
        return changingActions(currentConfig -> {
            final GatewayConfiguration mrc =
                    currentConfig.getEntities(GatewayConfiguration.class).get(name);
            if (mrc != null) {
                mrc.setParameters(object);
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Returns certain parameter for certain configured resource by its name.
     *
     * @return String value of the parameter
     */
    @GET
    @Path("/{name}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getParameterByName(@PathParam("name") final String name,
                                     @PathParam("paramName") final String paramName) {
        return readOnlyActions(currentConfig -> {
            final GatewayConfiguration mrc =
                    currentConfig.getEntities(GatewayConfiguration.class).get(name);
            if (mrc != null) {
                return mrc.getParameters().get(paramName);
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Returns certain parameter for certain configured resource by its name.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setParameterByName(@PathParam("name") final String name,
                                       @PathParam("paramName") final String paramName,
                                       final String value) {
        return changingActions(currentConfig -> {
            final GatewayConfiguration mrc =
                    currentConfig.getEntities(GatewayConfiguration.class).get(name);
            if (mrc != null) {
                mrc.getParameters().put(paramName, value);
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Removes certain parameter for certain configured resource by its name.
     *
     * @return no content response
     */
    @DELETE
    @Path("/{name}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeParameterByName(@PathParam("name") final String name,
                                          @PathParam("paramName") final String paramName) {
        return changingActions(currentConfig -> {
            final GatewayConfiguration mrc =
                    currentConfig.getEntities(GatewayConfiguration.class).get(name);
            if (mrc == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            } else {
                return mrc.getParameters().remove(paramName) != null;
            }
        });
    }
}