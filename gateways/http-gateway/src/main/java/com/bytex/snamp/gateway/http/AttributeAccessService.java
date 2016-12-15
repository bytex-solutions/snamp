package com.bytex.snamp.gateway.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Set;

@Singleton
@Path("/")
public final class AttributeAccessService {
    private final AttributeSupport attributes;
    private final Gson formatter;

    AttributeAccessService(final AttributeSupport registeredAttributes){
        this.attributes = Objects.requireNonNull(registeredAttributes);
        this.formatter = new Gson();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(HttpAttributeAccessor.ATTRIBUTE_ACCESS_PATH)
    public String getAttribute(@PathParam(HttpAttributeAccessor.RESOURCE_URL_PARAM) final String resourceName,
                                     @PathParam(HttpAttributeAccessor.ATTRIBUTE_URL_PARAM) final String attributeName) throws WebApplicationException{
        return attributes.getAttribute(resourceName, attributeName);
    }

    /**
     * Returns a list of available attributes.
     * @param resourceName The name of the requested resource.
     * @return JSON list of available attributes.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/attributes")
    public String getAttributes(@QueryParam("resource") final String resourceName){
        if(resourceName == null || resourceName.isEmpty()){   //all resources with attributes
            final JsonObject result = new JsonObject();
            for(final String resource: attributes.getHostedResources())
                result.add(resource, formatter.toJsonTree(attributes.getResourceAttributes(resource).stream().toArray(String[]::new)));
            return formatter.toJson(result);
        }
        else {
            final Set<String> result = attributes.getResourceAttributes(resourceName);
            return formatter.toJson(result.stream().toArray(String[]::new));
        }
    }

    @POST
    @Path(HttpAttributeAccessor.ATTRIBUTE_ACCESS_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setAttribute(@PathParam(HttpAttributeAccessor.RESOURCE_URL_PARAM)final String resourceName,
                                   @PathParam(HttpAttributeAccessor.ATTRIBUTE_URL_PARAM)final String attributeName,
                                   final String attributeValue) throws WebApplicationException {
        attributes.setAttribute(resourceName, attributeName, attributeValue);
        return Response.noContent().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources")
    public String getResources() {
        return formatter.toJson(attributes.getHostedResources().stream().toArray(String[]::new));
    }
}