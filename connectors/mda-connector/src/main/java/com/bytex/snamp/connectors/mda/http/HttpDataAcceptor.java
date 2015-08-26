package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.VolatileBox;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AbstractAttributeSupport;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.connectors.mda.SimpleTimer;
import com.bytex.snamp.connectors.notifications.AbstractNotificationSupport;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvoker;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvokerFactory;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.hazelcast.core.HazelcastInstance;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.mda.MdaResourceConfigurationDescriptorProvider.parseType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
@Singleton
public final class HttpDataAcceptor extends AbstractManagedResourceConnector implements DataAcceptor {
    private static final class MDANotificationSupport extends AbstractNotificationSupport<HttpNotificationAccessor>{
        private static final Class<HttpNotificationAccessor> FEATURE_TYPE = HttpNotificationAccessor.class;
        private final Logger logger;
        private final Gson jsonFormatter;
        private final NotificationListenerInvoker listenerInvoker;
        private final SimpleTimer lastWriteAccess;

        private MDANotificationSupport(final String resourceName,
                                       final SimpleTimer lastWriteAccess,
                                       final Logger logger,
                                       final Gson formatter,
                                       final ExecutorService threadPool){
            super(resourceName, FEATURE_TYPE);
            this.logger = Objects.requireNonNull(logger);
            this.jsonFormatter = Objects.requireNonNull(formatter);
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
            return new HttpNotificationAccessor(notifType, metadata, jsonFormatter);
        }

        private void fire(final String category, final JsonObject notification) throws JsonParseException {

            fire(new NotificationCollector() {
                @Override
                protected void process(final HttpNotificationAccessor metadata) {
                    if (category.equals(metadata.getDescriptor().getNotificationCategory()))
                        enqueue(metadata,
                                metadata.getMessage(notification),
                                metadata.getSequenceNumber(notification),
                                metadata.getTimeStamp(notification),
                                metadata.getUserData(notification));
                }
            });
            lastWriteAccess.reset();
        }

        private void fire(final String category, final String notification) throws JsonParseException{
            final JsonElement notif = jsonFormatter.fromJson(notification, JsonElement.class);
            if(notif != null && notif.isJsonObject())
                fire(category, notif.getAsJsonObject());
            else throw new JsonParseException("JSON Object expected");
        }

        @Override
        protected void failedToEnableNotifications(final String listID, final String category, final Exception e) {
            failedToEnableNotifications(logger, Level.WARNING, listID, category, e);
        }
    }

    private static final class MDAAttributeSupport extends AbstractAttributeSupport<HttpAttributeAccessor>{
        private static final Class<HttpAttributeAccessor> FEATURE_TYPE = HttpAttributeAccessor.class;
        private final ConcurrentMap<String, Object> storage;
        private final Logger logger;
        private final Cache<String, HttpAttributeManager> parsers;
        private final long expirationTime;
        private final SimpleTimer lastWriteAccess;

        private MDAAttributeSupport(final String resourceName,
                                    final long expirationTime,
                                    final SimpleTimer lastWriteAccess,
                                    final Logger logger) {
            super(resourceName, FEATURE_TYPE);
            this.logger = Objects.requireNonNull(logger);
            //try to discover hazelcast
            storage = createStorage(resourceName, Utils.getBundleContextByObject(this));
            parsers = CacheBuilder.newBuilder().weakValues().build();
            this.expirationTime = expirationTime;
            this.lastWriteAccess = Objects.requireNonNull(lastWriteAccess);
        }

        private static ConcurrentMap<String, Object> createStorage(final String resourceName,
                                                                   final BundleContext context){
            final ServiceReference<HazelcastInstance> hazelcast = context.getServiceReference(HazelcastInstance.class);
            if(hazelcast == null) //local storage
                return Maps.newConcurrentMap();
            else {
                final ServiceHolder<HazelcastInstance> holder = new ServiceHolder<>(context, hazelcast);
                try{
                    return holder.get().getMap(resourceName);
                }
                finally {
                    holder.release(context);
                }
            }
        }

        @Override
        protected HttpAttributeAccessor connectAttribute(final String attributeID,
                                                            final AttributeDescriptor descriptor) throws JMException {
            final OpenType<?> attributeType = parseType(descriptor);
            HttpAttributeManager result = parsers.getIfPresent(descriptor.getAttributeName());
            if (result != null){
                //do nothing
            }
            else if (attributeType instanceof SimpleType<?>) {
                result = new SimpleAttributeManager(WellKnownType.getType(attributeType), descriptor.getAttributeName());
                result.saveTo(parsers);
            }
            else if (attributeType instanceof CompositeType) {
                result = new CompositeAttributeManager((CompositeType) attributeType, descriptor.getAttributeName());
                result.saveTo(parsers);
            } else {
                result = new FallbackAttributeManager(descriptor.getAttributeName());
                result.saveTo(parsers);
            }
            final HttpAttributeAccessor accessor = new HttpAttributeAccessor(attributeID, attributeType, descriptor, result);
            accessor.setValue(accessor.getDefaultValue(), storage);
            return accessor;
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.WARNING, attributeID, attributeName, e);
        }

        @Override
        protected Object getAttribute(final HttpAttributeAccessor metadata) {
            if(lastWriteAccess.checkInterval(expirationTime, TimeUnit.MILLISECONDS) > 0)
                throw new IllegalStateException("Attribute value is too old. Backend component must supply a fresh value");
            else
                return metadata.getValue(storage);
        }

        private String getAttribute(final String attributeName, final Gson formatter) throws AttributeNotFoundException{
            final HttpAttributeManager attribute = parsers.getIfPresent(attributeName);
            if(attribute == null)
                throw JMExceptionUtils.attributeNotFound(attributeName);
            else return attribute.getValue(formatter, storage);
        }

        private String setAttribute(final String attributeName, final Gson formatter, final String value) throws AttributeNotFoundException, InvalidAttributeValueException, OpenDataException {
            final HttpAttributeManager attribute = parsers.getIfPresent(attributeName);
            if(attribute == null)
                throw JMExceptionUtils.attributeNotFound(attributeName);
            else {
                final String result = attribute.setValue(value, formatter, storage);
                lastWriteAccess.reset();
                return result;
            }
        }

        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(logger, Level.SEVERE, attributeID, e);
        }

        @Override
        protected void setAttribute(final HttpAttributeAccessor attribute, final Object value) throws InvalidAttributeValueException {
            attribute.setValue(value, storage);
            lastWriteAccess.reset();
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.SEVERE, attributeID, value, e);
        }
    }

    private final Gson formatter;
    private final MDAAttributeSupport attributes;
    private final MDANotificationSupport notifications;
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
        this.attributes = new MDAAttributeSupport(resourceName, expirationTime, lastWriteAccess, getLogger());
        this.notifications = new MDANotificationSupport(resourceName, lastWriteAccess, getLogger(), formatter, threadPool);
    }

    private Response setAttributes(final JsonObject items){
        final JsonObject result = new JsonObject();
        for(final Map.Entry<String, JsonElement> attribute: items.entrySet())
            try {
                final String previous = attributes.setAttribute(attribute.getKey(),
                        formatter,
                        formatter.toJson(attribute.getValue())
                );
                result.add(attribute.getKey(), formatter.fromJson(previous, JsonElement.class));
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
            final String result = attributes.setAttribute(attributeName, formatter, value);
            return Response
                    .ok(result, MediaType.APPLICATION_JSON_TYPE)
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
                               @Context final UriInfo info) throws WebApplicationException{
        try {
            return attributes.getAttribute(attributeName, formatter);
        } catch (final AttributeNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        } catch (final Exception e){
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/notifications/{category}")
    public void sendNotification(@PathParam("category")final String category, final String notification) throws WebApplicationException{
        try {
            notifications.fire(category, notification);
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
        attributes.removeAll(true);
        attributes.parsers.invalidateAll();
        notifications.removeAll(true, true);
        super.close();
    }
}
