package com.itworks.snamp.adapters.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.adapters.ReadAttributeLogicalOperation;
import com.itworks.snamp.adapters.WriteAttributeLogicalOperation;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/")
@Singleton
public final class RestAdapterService {
    private static final String RESOURCE_NAME_PARAM = "resourceName";
    private static final String ATTRIBUTE_NAME_PARAM = "attributeName";

    private final HttpAttributesModel attributes;
    private final boolean securityEnabled;

    RestAdapterService(final HttpAttributesModel registeredAttributes, final boolean securityEnabled){
        this.attributes = registeredAttributes;
        this.securityEnabled = securityEnabled;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{" + RESOURCE_NAME_PARAM + "}/{" + ATTRIBUTE_NAME_PARAM + "}")
    public final String getAttribute(@PathParam(RESOURCE_NAME_PARAM) final String resourceName,
                                     @PathParam(ATTRIBUTE_NAME_PARAM) final String attributeName,
                                     @Context final SecurityContext context){
        if(securityEnabled) RestAdapterHelpers.wellKnownRoleRequired(context);
        final HttpAttributeMapping attr = attributes.get(resourceName, attributeName);
        if(attr != null)
            try(final LogicalOperation ignored = new ReadAttributeLogicalOperation(attr.getName(), attributeName)){
                return attr.getValue();
            }
            catch (final Exception e){
                throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
            }
        else throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    /**
     * Returns a list of available attributes.
     * @return JSON list of available attributes.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public final String getAttributes(@Context final SecurityContext context){
        if(securityEnabled) RestAdapterHelpers.wellKnownRoleRequired(context);
        final JsonArray result = new JsonArray();
        for(final String attributeID: attributes.keySet())
            result.add(new JsonPrimitive(attributeID));
        return attributes.getJsonFormatter().toJson(result);
    }

    @ThreadSafe
    @POST
    @Path("/{" + RESOURCE_NAME_PARAM + "}/{" + ATTRIBUTE_NAME_PARAM + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public final String setAttribute(@PathParam(RESOURCE_NAME_PARAM)final String resourceName,
                                   @PathParam(ATTRIBUTE_NAME_PARAM)final String attributeName,
                                   final String attributeValue,
                                   @Context SecurityContext context) {
        if (securityEnabled) RestAdapterHelpers.maintainerRequired(context);
        final HttpAttributeMapping attr = attributes.get(resourceName, attributeName);
        if (attr != null)
            try(final LogicalOperation ignored = new WriteAttributeLogicalOperation(attr.getName(), attributeName)) {
                attr.setValue(attributeValue);
                return attributes.getJsonFormatter().toJson(new JsonPrimitive(true));
            } catch (final Exception e) {
                throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
            }
        else throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}