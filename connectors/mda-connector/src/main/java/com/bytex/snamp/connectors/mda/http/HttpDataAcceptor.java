package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.VolatileBox;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.connectors.mda.MDAAttributeRepository;
import com.bytex.snamp.connectors.mda.SimpleTimer;
import com.bytex.snamp.connectors.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvoker;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvokerFactory;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.*;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider.parseType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
@Singleton
public final class HttpDataAcceptor extends AbstractManagedResourceConnector implements DataAcceptor {
    private static final class HttpNotificationRepository extends AbstractNotificationRepository<HttpNotificationAccessor> {
        private static final Class<HttpNotificationAccessor> FEATURE_TYPE = HttpNotificationAccessor.class;
        private final Logger logger;
        private final NotificationListenerInvoker listenerInvoker;
        private final SimpleTimer lastWriteAccess;

        private HttpNotificationRepository(final String resourceName,
                                           final SimpleTimer lastWriteAccess,
                                           final Logger logger,
                                           final ExecutorService threadPool){
            super(resourceName, FEATURE_TYPE);
            this.logger = Objects.requireNonNull(logger);
            this.lastWriteAccess = Objects.requireNonNull(lastWriteAccess);
            listenerInvoker =
                    NotificationListenerInvokerFactory.createParallelInvoker(threadPool);
        }

        /**
         * Gets the invoker used to executed notification listeners.
         *
         * @return The notification listener invoker.
         */
        @Override
        protected NotificationListenerInvoker getListenerInvoker() {
            return listenerInvoker;
        }

        @Override
        protected HttpNotificationAccessor enableNotifications(final String notifType,
                                                              final NotificationDescriptor metadata) throws OpenDataException{
            return new HttpNotificationAccessor(notifType, metadata);
        }

        private void fire(final String category, final JsonObject notification, final Gson formatter) throws JsonParseException {

            fire(new NotificationCollector() {
                @Override
                protected void process(final HttpNotificationAccessor metadata) {
                    if (category.equals(metadata.getDescriptor().getNotificationCategory()))
                        try {
                            enqueue(metadata,
                                    metadata.getMessage(notification),
                                    metadata.getSequenceNumber(notification),
                                    metadata.getTimeStamp(notification),
                                    metadata.getUserData(notification, formatter));
                        } catch (final OpenDataException e) {
                            logger.log(Level.SEVERE, "Unable to process notification " + notification, e);
                        }
                }
            });
            lastWriteAccess.reset();
        }

        private void fire(final String category, final String notification, final Gson formatter) throws JsonParseException{
            final JsonElement notif = formatter.fromJson(notification, JsonElement.class);
            if(notif != null && notif.isJsonObject())
                fire(category, notif.getAsJsonObject(), formatter);
            else throw new JsonParseException("JSON Object expected");
        }

        @Override
        protected void failedToEnableNotifications(final String listID, final String category, final Exception e) {
            failedToEnableNotifications(logger, Level.WARNING, listID, category, e);
        }
    }

    private static final class HttpAttributeRepository extends MDAAttributeRepository<HttpAttributeAccessor> implements SafeCloseable {
        private static final Class<HttpAttributeAccessor> FEATURE_TYPE = HttpAttributeAccessor.class;
        private final Cache<String, HttpValueParser> parsers;

        private HttpAttributeRepository(final String resourceName,
                                        final long expirationTime,
                                        final SimpleTimer lastWriteAccess,
                                        final Logger logger) {
            super(resourceName, FEATURE_TYPE, expirationTime, lastWriteAccess, logger);
            parsers = CacheBuilder.newBuilder().weakValues().build();
        }

        @Override
        protected HttpAttributeAccessor connectAttribute(final String attributeID,
                                                            final AttributeDescriptor descriptor) throws JMException {
            final OpenType<?> attributeType = parseType(descriptor);
            HttpValueParser result = parsers.getIfPresent(descriptor.getAttributeName());
            if (result != null){
                //do nothing
            }
            else if (attributeType instanceof SimpleType<?> || attributeType instanceof ArrayType<?>)
                HttpAttributeAccessor.saveParser(result = new SimpleValueParser(WellKnownType.getType(attributeType)),
                        descriptor,
                        parsers);
            else if (attributeType instanceof CompositeType)
                HttpAttributeAccessor.saveParser(result = new CompositeValueParser((CompositeType) attributeType),
                        descriptor,
                        parsers);
            else
                HttpAttributeAccessor.saveParser(result = FallbackValueParser.INSTANCE, descriptor, parsers);
            final HttpAttributeAccessor accessor = new HttpAttributeAccessor(attributeID, attributeType, descriptor, result);
            accessor.setValue(accessor.getDefaultValue(), storage);
            return accessor;
        }

        private JsonElement getAttribute(final String attributeName, final Gson formatter) throws AttributeNotFoundException{
            final HttpValueParser parser = parsers.getIfPresent(attributeName);
            if(parser == null)
                throw JMExceptionUtils.attributeNotFound(attributeName);
            else return HttpAttributeAccessor.getValue(attributeName, parser, formatter, storage);
        }

        private JsonElement setAttribute(final String attributeName, final Gson formatter, final JsonElement value) throws AttributeNotFoundException, InvalidAttributeValueException, OpenDataException {
            final HttpValueParser parser = parsers.getIfPresent(attributeName);
            if(parser == null)
                throw JMExceptionUtils.attributeNotFound(attributeName);
            else {
                final JsonElement result = HttpAttributeAccessor.setValue(attributeName, parser, value, formatter, storage);
                lastWriteAccess.reset();
                return result;
            }
        }

        @Override
        protected Object getDefaultValue(final String storageName) {
            final HttpValueParser parser = parsers.getIfPresent(storageName);
            return parser != null ? parser.getDefaultValue() : null;
        }

        @Override
        public void close() {
            super.close();
            parsers.invalidateAll();
            parsers.cleanUp();
        }
    }

    private final Gson formatter;
    private final HttpAttributeRepository attributes;
    private final HttpNotificationRepository notifications;
    private final String servletContext;
    private final ExecutorService threadPool;
    private final VolatileBox<HttpService> publisherRef;

    HttpDataAcceptor(final String resourceName,
                     final String context,
                     final long expirationTime,
                     final Supplier<? extends ExecutorService> threadPoolFactory) {
        this.servletContext = Objects.requireNonNull(context);
        this.publisherRef = new VolatileBox<>();
        this.threadPool = threadPoolFactory.get();
        this.formatter = JsonUtils.registerTypeAdapters(new GsonBuilder().serializeNulls()).create();

        final SimpleTimer lastWriteAccess = new SimpleTimer();
        this.attributes = new HttpAttributeRepository(resourceName, expirationTime, lastWriteAccess, getLogger());
        this.notifications = new HttpNotificationRepository(resourceName, lastWriteAccess, getLogger(), threadPool);
    }

    private Response setAttributes(final JsonObject items){
        final JsonObject result = new JsonObject();
        for(final Map.Entry<String, JsonElement> attribute: items.entrySet())
            try {
                final JsonElement previous = attributes.setAttribute(attribute.getKey(),
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
            } catch (final InvalidAttributeValueException | JsonParseException e) {
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
    public void reset(){
        this.attributes.reset();
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
            final JsonElement result = attributes.setAttribute(attributeName, formatter, formatter.fromJson(value, JsonElement.class));
            return Response
                    .ok(formatter.toJson(result), MediaType.APPLICATION_JSON_TYPE)
                    .build();
        } catch (final AttributeNotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .build();
        } catch (final InvalidAttributeValueException | JsonParseException e) {
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
            return formatter.toJson(attributes.getAttribute(attributeName, formatter));
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
            notifications.fire(category, notification, formatter);
        }
        catch (final JsonParseException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e){
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, notifications);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, notifications);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType, new Function<Class<T>, T>() {
            @Override
            public T apply(final Class<T> objectType) {
                return HttpDataAcceptor.super.queryObject(objectType);
            }
        }, attributes, notifications);
    }

    @Override
    public boolean addAttribute(final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        return attributes.addAttribute(attributeID, attributeName, readWriteTimeout, options) != null;
    }

    @Override
    public void removeAttributesExcept(final Set<String> attributes) {
        this.attributes.removeAllExcept(attributes);
    }

    @Override
    public boolean enableNotifications(final String listId, final String category, final CompositeData options) {
        return notifications.enableNotifications(listId, category, options) != null;
    }

    @Override
    public void disableNotificationsExcept(final Set<String> notifications) {
        this.notifications.removeAllExcept(notifications);
    }

    private void beginAccept(final HttpService publisher) throws IOException {
        publisherRef.set(publisher);
        try {
            publisher.registerServlet(servletContext, new MdaServlet(this), null, null);
        } catch (final ServletException | NamespaceException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void beginAccept(final Object... dependencies) throws IOException{
        for(final Object dep: dependencies)
            if(dep instanceof HttpService){
                beginAccept((HttpService)dep);
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
        if(publisher != null)
            publisher.unregister(servletContext);
        threadPool.shutdown();
        attributes.close();
        notifications.removeAll(true, true);
        super.close();
    }
}
