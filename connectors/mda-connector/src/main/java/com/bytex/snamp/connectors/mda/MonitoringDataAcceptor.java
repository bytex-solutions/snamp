package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AbstractAttributeSupport;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.hazelcast.core.HazelcastInstance;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
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
public final class MonitoringDataAcceptor extends AbstractManagedResourceConnector {
    private static final class MDAAttributeSupport extends AbstractAttributeSupport<MdaAttributeAccessor>{
        private static final Class<MdaAttributeAccessor> FEATURE_TYPE = MdaAttributeAccessor.class;
        private final ConcurrentMap<String, Object> storage;
        private final Logger logger;
        private final Map<String, AttributeStorage> parsers;

        private MDAAttributeSupport(final String resourceName, final Logger logger) {
            super(resourceName, FEATURE_TYPE);
            this.logger = Objects.requireNonNull(logger);
            //try to discover hazelcast
            storage = createStorage(resourceName, Utils.getBundleContextByObject(this));
            parsers = Maps.newHashMapWithExpectedSize(20);
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
        protected MdaAttributeAccessor connectAttribute(final String attributeID,
                                                            final AttributeDescriptor descriptor) throws Exception {
            final OpenType<?> attributeType = parseType(descriptor);
            final AttributeStorage result;
            if (parsers.containsKey(descriptor.getAttributeName()))
                result = parsers.get(descriptor.getAttributeName());
            else if (attributeType instanceof SimpleType<?>) {
                result = new SimpleAttributeStorage((SimpleType<?>) attributeType, descriptor.getAttributeName());
                result.saveTo(parsers);
            } else if (attributeType instanceof CompositeType) {
                result = new CompositeAttributeStorage((CompositeType) attributeType, descriptor.getAttributeName());
                result.saveTo(parsers);
            } else {
                result = new FallbackAttributeStorage(descriptor.getAttributeName());
                result.saveTo(parsers);
            }
            final MdaAttributeAccessor accessor = new MdaAttributeAccessor(attributeID, attributeType, descriptor, result);
            accessor.setValue(accessor.getDefaultValue(), storage);
            return accessor;
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.WARNING, attributeID, attributeName, e);
        }

        @Override
        protected Object getAttribute(final MdaAttributeAccessor metadata) {
            return metadata.getValue(storage);
        }

        private String getAttribute(final String attributeName, final Gson formatter) throws AttributeNotFoundException{
            final AttributeStorage attribute = parsers.get(attributeName);
            if(attribute == null)
                throw JMExceptionUtils.attributeNotFound(attributeName);
            else return attribute.getValue(formatter, storage);
        }

        private String setAttribute(final String attributeName, final Gson formatter, final String value) throws AttributeNotFoundException, InvalidAttributeValueException, OpenDataException {
            final AttributeStorage attribute = parsers.get(attributeName);
            if(attribute == null)
                throw JMExceptionUtils.attributeNotFound(attributeName);
            else return attribute.setValue(value, formatter, storage);
        }

        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(logger, Level.SEVERE, attributeID, e);
        }

        @Override
        protected void setAttribute(final MdaAttributeAccessor attribute, final Object value) throws InvalidAttributeValueException {
            attribute.setValue(value, storage);
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.SEVERE, attributeID, value, e);
        }
    }

    final String resourceName;
    private final Gson formatter;
    private final MDAAttributeSupport attributes;

    MonitoringDataAcceptor(final String resourceName) {
        this.resourceName = resourceName;
        this.formatter = JsonUtils.registerTypeAdapters(new GsonBuilder().serializeNulls()).create();
        this.attributes = new MDAAttributeSupport(resourceName, getLogger());
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

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes);
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
                return MonitoringDataAcceptor.super.queryObject(objectType);
            }
        }, attributes);
    }

    boolean addAttribute(final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        return attributes.addAttribute(attributeID, attributeName, readWriteTimeout, options) != null;
    }

    void removeAttributesExcept(final Set<String> attributes) {
        this.attributes.removeAllExcept(attributes);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        attributes.removeAll(true);
        attributes.parsers.clear();
        super.close();
    }
}
