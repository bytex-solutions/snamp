package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.management.http.model.ManagedResourceDataObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Provides API for SNAMP resources management.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/configuration/resource")
public final class ResourceConfigurationService extends TemplateConfigurationService<ManagedResourceConfiguration, ManagedResourceDataObject> {
    ResourceConfigurationService(){
        super(ManagedResourceConfiguration.class);
    }

    @Override
    protected ManagedResourceDataObject toDataTransferObject(final ManagedResourceConfiguration entity) {
        return new ManagedResourceDataObject(entity);
    }

    @GET
    @Path("/{name}/connectionString")
    @Produces(MediaType.APPLICATION_JSON)
    public String getConnectionString(@PathParam("name") final String resourceName){
        return getConfigurationByName(resourceName, ManagedResourceConfiguration::getConnectionString);
    }

    @PUT
    @Path("/{name}/connectionString")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setConnectionString(@PathParam("name") final String resourceName, final String value){
        return setConfigurationByName(resourceName, config -> config.setConnectionString(value));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}/group")
    public String getGroupName(@PathParam("name") final String resourceName){
        return getConfigurationByName(resourceName, ManagedResourceConfiguration::getGroupName);
    }

    @Path("/{name}/group")
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setGroupName(@PathParam("name") final String resourceName, final String value){
        return setConfigurationByName(resourceName, config -> config.setGroupName(value));
    }
}
