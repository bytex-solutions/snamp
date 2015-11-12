package com.bytex.snamp.connectors.mda.impl.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.VolatileBox;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.google.common.base.Supplier;
import com.google.gson.*;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.*;
import javax.servlet.ServletException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
@Singleton
public final class HttpDataAcceptor extends DataAcceptor {

    private final Gson formatter;
    private final String servletContext;
    private final ExecutorService threadPool;
    private final VolatileBox<HttpService> publisherRef;
    private final HttpAttributeRepository attributes;
    private final HttpNotificationRepository notifications;

    HttpDataAcceptor(final String resourceName,
                     final String context,
                     final Supplier<? extends ExecutorService> threadPoolFactory) {
        this.threadPool = threadPoolFactory.get();
        this.attributes = new HttpAttributeRepository(resourceName, getLogger());
        this.notifications = new HttpNotificationRepository(resourceName,
                threadPool,
                Utils.getBundleContextByObject(this),
                getLogger());
        this.servletContext = Objects.requireNonNull(context);
        this.publisherRef = new VolatileBox<>();
        this.formatter = JsonUtils.registerTypeAdapters(new GsonBuilder().serializeNulls()).create();
    }

    @Override
    protected HttpAttributeRepository getAttributes() {
        return attributes;
    }

    @Override
    protected HttpNotificationRepository getNotifications() {
        return notifications;
    }

    private Response setAttributes(final JsonObject items){
        final JsonObject result = new JsonObject();
        for(final Map.Entry<String, JsonElement> attribute: items.entrySet())
            try {
                final JsonElement previous = getAttributes().setAttribute(attribute.getKey(),
                        formatter,
                        attribute.getValue()
                );
                result.add(attribute.getKey(), previous);
            } catch (final AttributeNotFoundException e) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(e.getMessage())
                        .type(MediaType.TEXT_PLAIN_TYPE)
                        .build();
            } catch (final OpenDataException | JsonParseException e) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(e.getMessage())
                        .type(MediaType.TEXT_PLAIN_TYPE)
                        .build();
            } catch (final Exception e){
                return Response
                        .serverError()
                        .entity(e.toString())
                        .type(MediaType.TEXT_PLAIN_TYPE)
                        .build();
            }
        return Response
                .ok(formatter.toJson(result), MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    /**
     * Sets attributes in batch manner.
     * @param value JSON Object with a set of attributes.
     * @return HTTP response.
     */
    @PUT
    @Path("/attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setAttributes(final String value){
        final JsonElement items = formatter.fromJson(value, JsonElement.class);
        if(items.isJsonObject())
            return setAttributes(items.getAsJsonObject());
        else return Response.status(Response.Status.BAD_REQUEST)
                        .entity("JSON Object expected")
                        .build();
    }

    @DELETE
    @Path("/attributes")
    public void reset() throws InvalidAttributeValueException, OpenDataException {
        getAttributes().reset();
    }

    /**
     * Sets value of the single attribute.
     * @param attributeName Attribute name.
     * @param value Attribute value in JSON format.
     * @return HTTP response.
     */
    @PUT
    @Path("/attributes/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @SpecialUse
    public Response setAttribute(@PathParam("name")final String attributeName,
                             final String value){
        try {
            final JsonElement result = getAttributes().setAttribute(attributeName, formatter, formatter.fromJson(value, JsonElement.class));
            return Response
                    .ok(formatter.toJson(result), MediaType.APPLICATION_JSON_TYPE)
                    .build();
        } catch (final AttributeNotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .build();
        } catch (final OpenDataException | JsonParseException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .build();
        } catch (final Exception e){
            return Response
                    .serverError()
                    .entity(e.toString())
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .build();
        }
    }

    /**
     * Gets attribute value in JSON format.
     * @param attributeName Attribute name.
     * @return Attribute value in JSON format.
     * @throws WebApplicationException Unable to get attribute value.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/attributes/{name}")
    public String getAttribute(@PathParam("name") final String attributeName,
                               @Context final UriInfo info) throws WebApplicationException {
        try {
            return formatter.toJson(getAttributes().getAttribute(attributeName, formatter));
        } catch (final AttributeNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        } catch (final Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/notifications/{category}")
    public void sendNotification(@PathParam("category")final String category, final String notification) throws WebApplicationException{
        try {
            getNotifications().fire(category, notification, formatter);
        }
        catch (final JsonParseException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e){
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private void beginListening(final HttpService publisher) throws IOException {
        publisherRef.set(publisher);
        try {
            publisher.registerServlet(servletContext, new MdaServlet(this), null, null);
        } catch (final ServletException | NamespaceException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void beginListening(final Object... dependencies) throws IOException{
        for(final Object dep: dependencies)
            if(dep instanceof HttpService){
                beginListening((HttpService)dep);
                return;
            }
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        final HttpService publisher = publisherRef.getAndSet(null);
        if (publisher != null)
            publisher.unregister(servletContext);
        threadPool.shutdown();
        attributes.close();
        notifications.close();
        super.close();
    }
}
