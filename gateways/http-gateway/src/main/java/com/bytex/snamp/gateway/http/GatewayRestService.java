package com.bytex.snamp.gateway.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.spi.resource.Singleton;
import org.atmosphere.annotation.Suspend;
import org.atmosphere.jersey.SuspendResponse;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Set;
import static com.bytex.snamp.internal.Utils.callAndWrapException;

@Path("/")
@Singleton
@org.atmosphere.config.service.Singleton
public final class GatewayRestService {
    private final AttributeSupport attributes;
    private final Gson formatter;
    private final NotificationSupport notifications;

    GatewayRestService(final AttributeSupport registeredAttributes,
                       final NotificationSupport notifications){
        this.attributes = Objects.requireNonNull(registeredAttributes);
        this.formatter = new Gson();
        this.notifications = Objects.requireNonNull(notifications);
    }

    @GET
    @Path(HttpNotificationAccessor.NOTIFICATION_ACCESS_PATH)
    @Suspend(contentType = MediaType.APPLICATION_JSON)
    public SuspendResponse<String> subscribe(@PathParam(HttpNotificationAccessor.RESOURCE_URL_PARAM) final String resourceName,
                                             @Context final HttpServletRequest request) throws WebApplicationException {
        final InternalBroadcaster broadcaster = notifications.getBroadcaster(resourceName);
        if (broadcaster != null)
            return callAndWrapException(() -> {
                broadcaster.initialize(request);
                return new SuspendResponse.SuspendResponseBuilder<String>()
                        .broadcaster(broadcaster)
                        .build();
            }, WebApplicationException::new);
        else
            throw new WebApplicationException(new IllegalArgumentException(String.format("Unknown resource %s", resourceName)), Response.Status.NOT_FOUND);
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