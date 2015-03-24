package com.itworks.snamp.adapters.http;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.itworks.snamp.adapters.*;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.json.*;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.jersey.JerseyBroadcaster;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.management.*;
import javax.management.openmbean.*;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.*;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static org.atmosphere.cpr.FrameworkConfig.ATMOSPHERE_CONFIG;

/**
 * Represents HTTP adapter that exposes management information through HTTP and WebSocket to the outside world.
 * This class cannot be inherited.
 * @author Roman Sakno
 */
final class HttpAdapter extends AbstractResourceAdapter {
    static final String NAME = HttpAdapterHelpers.ADAPTER_NAME;
    private static final int METHOD_NOT_ALLOWED = 405;

    private static final class HttpAttributeMapping extends AttributeAccessor {
        private Gson formatter;

        private HttpAttributeMapping(final MBeanAttributeInfo attributeInfo){
            super(attributeInfo);
            final String dateFormat = HttpAdapterConfigurationDescriptor.getDateFormatParam(getMetadata().getDescriptor());
            GsonBuilder builder = new GsonBuilder();
            if(dateFormat != null && dateFormat.length() > 0)
                builder = builder.setDateFormat(dateFormat);
            if(getType() != null)
                switch (getType()){
                    case BYTE_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new ByteBufferFormatter());
                        break;
                    case CHAR_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new CharBufferFormatter());
                        break;
                    case SHORT_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new ShortBufferFormatter());
                        break;
                    case INT_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new IntBufferFormatter());
                        break;
                    case LONG_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new LongBufferFormatter());
                        break;
                    case FLOAT_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new FloatBufferFormatter());
                        break;
                    case DOUBLE_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new DoubleBufferFormatter());
                        break;
                    case OBJECT_NAME_ARRAY:
                    case OBJECT_NAME:
                        builder = builder.registerTypeAdapter(ObjectName.class, new ObjectNameFormatter());
                        break;
                    case DICTIONARY:
                        CompositeType compositeType = (CompositeType) getOpenType();
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new CompositeDataFormatter(compositeType));
                        break;
                    case TABLE:
                        TabularType tabularType = (TabularType)getOpenType();
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new TabularDataFormatter(tabularType));
                        break;
                    case DICTIONARY_ARRAY:
                        compositeType = (CompositeType)((ArrayType<?>)getOpenType()).getElementOpenType();
                        builder = builder.registerTypeAdapter(getType().getJavaType(), new ArrayOfCompositeDataFormatter(compositeType));
                        break;
                    case TABLE_ARRAY:
                        tabularType = (TabularType)((ArrayType<?>)getOpenType()).getElementOpenType();
                        builder = builder.registerTypeHierarchyAdapter(getType().getJavaType(), new ArrayOfTabularDataFormatter(tabularType));
                        break;
                }
            formatter = builder.create();
        }

        private JsonElement getValueAsJson() throws WebApplicationException {
            try {
                return formatter.toJsonTree(getValue());
            } catch (final AttributeNotFoundException e) {
                throw new WebApplicationException(e, Response.Status.NOT_FOUND);
            }
            catch (final Exception e){
                throw new WebApplicationException(e);
            }
        }

        private void setValue(final JsonElement json) throws WebApplicationException {
            if (getType() != null)
                try {
                    setValue(formatter.fromJson(json, getType()));
                } catch (final AttributeNotFoundException e) {
                    throw new WebApplicationException(e, Response.Status.NOT_FOUND);
                } catch (final InvalidAttributeValueException | JsonParseException e) {
                    throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
                } catch (final Exception e) {
                    throw new WebApplicationException(e);
                }
            else throw new WebApplicationException(METHOD_NOT_ALLOWED);
        }

        private String toString(final JsonElement json) {
            return formatter.toJson(json);
        }

        private JsonElement fromString(final String json) {
            return formatter.fromJson(json, JsonElement.class);
        }
    }

    private static final class HttpAttributesModel extends AbstractAttributesModel<HttpAttributeMapping> implements AttributeSupport{

        private static WebApplicationException attributeNotFound(final String resourceName,
                                                                 final String attributeName){
            return new WebApplicationException(new AttributeNotFoundException(String.format("Attribute %s in resource %s no longer accessible", attributeName, resourceName)),
                    Response.Status.NOT_FOUND);
        }

        @Override
        public String getAttribute(final String resourceName, final String attributeName) throws WebApplicationException {
            try(final LockScope ignored = beginRead()){
                final HttpAttributeMapping mapping = get(resourceName, attributeName);
                if(mapping != null)
                    return mapping.toString(mapping.getValueAsJson());
                else throw attributeNotFound(resourceName, attributeName);
            }
        }

        @Override
        public void setAttribute(final String resourceName, final String attributeName, final String value) throws WebApplicationException {
            try(final LockScope ignored = beginRead()){
                final HttpAttributeMapping mapping = get(resourceName, attributeName);
                if(mapping != null)
                    mapping.setValue(mapping.fromString(value));
                else throw attributeNotFound(resourceName, attributeName);
            }
        }

        @Override
        protected HttpAttributeMapping createAccessor(final MBeanAttributeInfo metadata) throws Exception {
            return new HttpAttributeMapping(metadata);
        }
    }

    private static final class NotificationEmitter extends NotificationAccessor {
        private final WeakReference<NotificationListener> listener;

        private NotificationEmitter(final MBeanNotificationInfo metadata,
                                    final NotificationListener listener) {
            super(metadata);
            this.listener = new WeakReference<>(listener);
        }

        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            final NotificationListener listener = this.listener.get();
            if(listener != null) listener.handleNotification(notification, handback);
        }
    }

    private static final class NotificationBroadcaster extends JerseyBroadcaster implements InternalBroadcaster, NotificationListener {
        private final ResourceNotificationList<NotificationEmitter> notifications;
        private final String resourceName;
        private final Gson formatter;

        private NotificationBroadcaster(final String resourceName,
                                        final Gson formatter){
            notifications = new ResourceNotificationList<>();
            this.resourceName = resourceName;
            this.formatter = Objects.requireNonNull(formatter);
        }

        private static URI getRequestURI(final HttpServletRequest request) throws URISyntaxException {
            return new URI(request.getRequestURL().toString());
        }

        @Override
        public void init(final HttpServletRequest request) throws URISyntaxException {
            if (request != null && !isInitialized() && !isDestroyed())
                synchronized (this) {
                    if (isInitialized() || isDestroyed()) return;
                    final AtmosphereConfig frameworkConfig = Utils.safeCast(request.getAttribute(ATMOSPHERE_CONFIG), AtmosphereConfig.class);
                    if (frameworkConfig == null)
                        throw new RuntimeException(String.format("%s resource broadcaster cannot catch Atmosphere Framework config", resourceName));
                    else initialize(resourceName, getRequestURI(request), frameworkConfig);
                }
        }

        private boolean isInitialized(){
            return initialized.get();
        }

        @Override
        public void handleNotification(final Notification notif, final Object handback){
            notif.setSource(resourceName);
            if(isInitialized() && !isDestroyed())
                broadcast(formatter.toJson(notif));
        }

        @Override
        public synchronized void destroy() {
            super.destroy();
        }

        private NotificationEmitter addNotification(final MBeanNotificationInfo metadata) {
            final NotificationEmitter emitter = new NotificationEmitter(metadata, this);
            notifications.put(emitter);
            return emitter;
        }

        private NotificationEmitter removeNotification(final MBeanNotificationInfo metadata) {
            return notifications.remove(metadata);
        }

        private boolean isEmpty(){
            return notifications.isEmpty();
        }

        private void clear() {
            for(final NotificationAccessor accessor: notifications.values())
                accessor.disconnect();
            notifications.clear();
        }
    }

    private static final class HttpNotificationsModel extends ThreadSafeObject implements NotificationSupport{
        private final KeyedObjects<String, NotificationBroadcaster> notifications;
        private final Gson formatter;

        private HttpNotificationsModel(){
            this.formatter = new GsonBuilder()
                    .registerTypeHierarchyAdapter(Notification.class, new NotificationSerializer())
                    .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter())
                    .registerTypeAdapter(ByteBuffer.class, new ByteBufferFormatter())
                    .registerTypeAdapter(CharBuffer.class, new CharBufferFormatter())
                    .registerTypeAdapter(ShortBuffer.class, new ShortBufferFormatter())
                    .registerTypeAdapter(IntBuffer.class, new IntBufferFormatter())
                    .registerTypeAdapter(LongBuffer.class, new LongBufferFormatter())
                    .registerTypeAdapter(FloatBuffer.class, new FloatBufferFormatter())
                    .registerTypeAdapter(DoubleBuffer.class, new DoubleBufferFormatter())
                    .registerTypeHierarchyAdapter(CompositeData.class, new CompositeDataSerializer())
                    .registerTypeHierarchyAdapter(TabularData.class, new TabularDataSerializer())
                    .create();
            this.notifications = createBroadcasters();
        }

        private static KeyedObjects<String, NotificationBroadcaster> createBroadcasters(){
            return new AbstractKeyedObjects<String, NotificationBroadcaster>(10) {
                private static final long serialVersionUID = 4673736468179786561L;

                @Override
                public String getKey(final NotificationBroadcaster item) {
                    return item.resourceName;
                }

                @Override
                public void clear() {
                    for(final NotificationBroadcaster broadcaster: values()){
                        broadcaster.clear();
                        broadcaster.destroy();
                    }
                    super.clear();
                }
            };
        }

        private NotificationEmitter addNotification(final String resourceName,
                                    final MBeanNotificationInfo metadata) {
            try (final LockScope ignored = beginWrite()) {
                final NotificationBroadcaster broadcaster;
                if (notifications.containsKey(resourceName))
                    broadcaster = notifications.get(resourceName);
                else notifications.put(broadcaster = new NotificationBroadcaster(resourceName, formatter));
                return broadcaster.addNotification(metadata);
            }
        }

        @Override
        public NotificationBroadcaster getBroadcaster(final String resourceName) {
            try (final LockScope ignored = beginRead()) {
                return notifications.get(resourceName);
            }
        }

        private NotificationAccessor removeNotification(final String resourceName,
                                                        final MBeanNotificationInfo metadata) {
            try(final LockScope ignored = beginWrite()){
                NotificationBroadcaster broadcaster;
                if(notifications.containsKey(resourceName))
                    broadcaster = notifications.get(resourceName);
                else return null;
                final NotificationAccessor acessor = broadcaster.removeNotification(metadata);
                if(broadcaster.isEmpty()) {
                    broadcaster = notifications.remove(resourceName);
                    broadcaster.destroy();
                }
                return acessor;
            }
        }

        private void clear() {
            try(final LockScope ignored = beginWrite()){
                notifications.clear();
            }
        }

        private Collection<? extends NotificationAccessor> clear(final String resourceName) {
            try(final LockScope ignored = beginWrite()){
                if(notifications.containsKey(resourceName)){
                    final NotificationBroadcaster broadcaster = notifications.remove(resourceName);
                    broadcaster.destroy();
                    return broadcaster.notifications.values();
                }
                else return ImmutableList.of();
            }
        }
    }

    private static final class JerseyServletFactory implements ServletFactory<ServletContainer> {
        private final HttpNotificationsModel notifications;
        private final HttpAttributesModel attributes;

        JerseyServletFactory(final HttpAttributesModel attributes, final HttpNotificationsModel notifications){
            this.notifications = Objects.requireNonNull(notifications);
            this.attributes = Objects.requireNonNull(attributes);
        }

        private static Application createResourceConfig(final AdapterRestService serviceInstance){
            final DefaultResourceConfig result = new DefaultResourceConfig();
            result.getSingletons().add(serviceInstance);
            return result;
        }

        @Override
        public ServletContainer get() {
            return new ServletContainer(createResourceConfig(new AdapterRestService(attributes, notifications)));
        }
    }

    private final HttpService publisher;
    private final JerseyServletFactory servletFactory;

    HttpAdapter(final String instanceName, final HttpService servletPublisher) {
        super(instanceName);
        publisher = Objects.requireNonNull(servletPublisher, "servletPublisher is null.");
        servletFactory = new JerseyServletFactory(new HttpAttributesModel(),
                new HttpNotificationsModel());
    }

    private String getServletContext(){
        final String SERVLET_CONTEXT = "/snamp/adapters/http/%s";
        return String.format(SERVLET_CONTEXT, getInstanceName());
    }

    @Override
    protected void start(final Map<String, String> parameters) throws ServletException, NamespaceException {
        final AtmosphereObjectFactoryBuilder objectFactory = new AtmosphereObjectFactoryBuilder()
                .add(Servlet.class, servletFactory);
        //register RestAdapterServlet as a OSGi service
        publisher.registerServlet(getServletContext(), new HttpAdapterServlet(objectFactory.build()), null, null);
    }

    @Override
    protected void stop() {
        //unregister RestAdapter Servlet as a OSGi service.
        publisher.unregister(getServletContext());
        servletFactory.attributes.clear();
        servletFactory.notifications.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>)servletFactory.attributes.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, S>)servletFactory.notifications.addNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) {
        final Collection<? extends AttributeAccessor> attributes =
                servletFactory.attributes.clear(resourceName);
        final Collection<? extends NotificationAccessor> notifications =
                servletFactory.notifications.clear(resourceName);
        return Iterables.concat(attributes, notifications);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, ?>)servletFactory.attributes.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, ?>)servletFactory.notifications.removeNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}
