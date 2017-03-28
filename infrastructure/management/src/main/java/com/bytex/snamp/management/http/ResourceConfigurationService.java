package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.management.http.model.ResourceDataObject;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Provides API for SNAMP resources management.
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/configuration/resource")
public final class ResourceConfigurationService extends TemplateConfigurationService<ManagedResourceConfiguration, ResourceDataObject> {
    @Override
    protected ResourceDataObject toDataTransferObject(final ManagedResourceConfiguration entity) {
        return new ResourceDataObject(entity);
    }

    /**
     * Gets connection string.
     *
     * @param resourceName the resource name
     * @return the connection string
     */
    @GET
    @Path("/{name}/connectionString")
    @Produces(MediaType.APPLICATION_JSON)
    public String getConnectionString(@PathParam("name") final String resourceName){
        return getConfigurationByName(resourceName, ManagedResourceConfiguration::getConnectionString);
    }

    /**
     * Sets connection string.
     *
     * @param resourceName the resource name
     * @param value        the value
     * @return the connection string
     */
    @PUT
    @Path("/{name}/connectionString")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setConnectionString(@PathParam("name") final String resourceName, final String value, @Context final SecurityContext context){
        return setConfigurationByName(resourceName, config -> config.setConnectionString(value), context);
    }

    /**
     * Gets group name.
     *
     * @param resourceName the resource name
     * @return the group name
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}/group")
    public String getGroupName(@PathParam("name") final String resourceName){
        return getConfigurationByName(resourceName, ManagedResourceConfiguration::getGroupName);
    }

    /**
     * Sets group name.
     *
     * @param resourceName the resource name
     * @param value        the value
     * @return the group name
     */
    @Path("/{name}/group")
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setGroupName(@PathParam("name") final String resourceName, final String value, @Context final SecurityContext context){
        return setConfigurationByName(resourceName, config -> config.setGroupName(value), context);
    }

    @Nonnull
    @Override
    public EntityMap<? extends ManagedResourceConfiguration> apply(@Nonnull final AgentConfiguration owner) {
        return owner.getResources();
    }
}
