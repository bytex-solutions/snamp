package com.itworks.snamp.adapters.http;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.WellKnownType;
import com.itworks.snamp.jmx.json.*;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.jersey.JerseyBroadcaster;
import org.osgi.service.http.HttpService;

import javax.management.*;
import javax.management.openmbean.*;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.*;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
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

    private static final class HttpAttributeMapping{
        private final AttributeAccessor accessor;
        private final WellKnownType attributeType;
        private Gson formatter;

        private HttpAttributeMapping(final AttributeAccessor accessor,
                                             final GsonBuilder builder){
            this.accessor = accessor;
            this.attributeType = accessor.getType();
            this.formatter = builder.create();
        }

        private String getName(){
            return accessor.getName();
        }

        private JsonElement getValue() throws WebApplicationException {
            try {
                return formatter.toJsonTree(accessor.getValue());
            } catch (final AttributeNotFoundException e) {
                throw new WebApplicationException(e, Response.Status.NOT_FOUND);
            }
            catch (final Exception e){
                throw new WebApplicationException(e);
            }
        }

        private boolean canRead() {
            return accessor.getMetadata().isReadable();
        }

        private boolean canWrite() {
            return attributeType != null && accessor.getMetadata().isWritable();
        }

        private void setValue(final JsonElement json) throws WebApplicationException {
            if (attributeType != null)
                try {
                    accessor.setValue(formatter.fromJson(json, attributeType.getType()));
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

    private static final class HttpAttributeManager extends AbstractKeyedObjects<String, HttpAttributeMapping>{
        private static final long serialVersionUID = 2767603193006584834L;
        private static char ATTR_NAME_SPLITTER = '/';
        private final String resourceName;

        private HttpAttributeManager(final String resourceName){
            super(10);
            this.resourceName = resourceName;
        }

        @Override
        public String getKey(final HttpAttributeMapping item) {
            return item.getName();
        }

        private String makeAttributeID(final String userDefinedName){
            return resourceName + ATTR_NAME_SPLITTER + userDefinedName;
        }

        /**
         * Removes all of the mappings from this map.
         * The map will be empty after this call returns.
         */
        @Override
        public void clear() {
            for(final HttpAttributeMapping attr: values())
                attr.accessor.disconnect();
            super.clear();
        }

        private String getAtttribute(final String userDefinedName) throws WebApplicationException{
            final String attributeID;
            if(containsKey(attributeID = makeAttributeID(userDefinedName))){
                final HttpAttributeMapping mapping = get(attributeID);
                if(mapping.canRead())
                    return mapping.toString(mapping.getValue());
                else throw new WebApplicationException(new IllegalStateException(String.format("Attribute %s is write-ony", userDefinedName)), METHOD_NOT_ALLOWED);
            }
            else throw new WebApplicationException(new IllegalArgumentException(String.format("Attribute %s doesn't exist", userDefinedName)), Response.Status.NOT_FOUND);
        }

        private void setAttribute(final String userDefinedName,
                                  final String value) {
            final String attributeID;
            if(containsKey(attributeID = makeAttributeID(userDefinedName))){
                final HttpAttributeMapping mapping = get(attributeID);
                if(mapping.canWrite())
                    mapping.setValue(mapping.fromString(value));
                else throw new WebApplicationException(new IllegalStateException(String.format("Attribute %s is read-only", userDefinedName)), METHOD_NOT_ALLOWED);
            }
            else throw new WebApplicationException(new IllegalArgumentException(String.format("Attribute %s doesn't exist", userDefinedName)), Response.Status.NOT_FOUND);
        }

        private HttpAttributeMapping createAttribute(final AttributeAccessor accessor){
            final WellKnownType type = accessor.getType();
            final String dateFormat = HttpAdapterConfigurationDescriptor.getDateFormatParam(accessor.getMetadata().getDescriptor());
            GsonBuilder builder = new GsonBuilder();
            if(dateFormat != null && dateFormat.length() > 0)
                builder = builder.setDateFormat(dateFormat);
            if(type != null)
                switch (type){
                    case BYTE_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new ByteBufferFormatter());
                    break;
                    case CHAR_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new CharBufferFormatter());
                    break;
                    case SHORT_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new ShortBufferFormatter());
                    break;
                    case INT_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new IntBufferFormatter());
                    break;
                    case LONG_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new LongBufferFormatter());
                    break;
                    case FLOAT_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new FloatBufferFormatter());
                    break;
                    case DOUBLE_BUFFER:
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new DoubleBufferFormatter());
                    break;
                    case OBJECT_NAME_ARRAY:
                    case OBJECT_NAME:
                        builder = builder.registerTypeAdapter(ObjectName.class, new ObjectNameFormatter());
                    break;
                    case DICTIONARY:
                        CompositeType compositeType = (CompositeType)accessor.getOpenType();
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new CompositeDataFormatter(compositeType));
                    break;
                    case TABLE:
                        TabularType tabularType = (TabularType)accessor.getOpenType();
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new TabularDataFormatter(tabularType));
                    break;
                    case DICTIONARY_ARRAY:
                        compositeType = (CompositeType)((ArrayType<?>)accessor.getOpenType()).getElementOpenType();
                        builder = builder.registerTypeAdapter(type.getType(), new ArrayOfCompositeDataFormatter(compositeType));
                    break;
                    case TABLE_ARRAY:
                        tabularType = (TabularType)((ArrayType<?>)accessor.getOpenType()).getElementOpenType();
                        builder = builder.registerTypeHierarchyAdapter(type.getType(), new ArrayOfTabularDataFormatter(tabularType));
                    break;
                }
            return new HttpAttributeMapping(accessor, builder);
        }

        private void addAttribute(final String userDefinedName,
                                  final AttributeConnector connector) throws JMException {
            final String attributeID = makeAttributeID(userDefinedName);
            put(createAttribute(connector.connect(attributeID)));
        }

        private AttributeAccessor removeAttribute(final String userDefinedName) {
            final String attributeID = makeAttributeID(userDefinedName);
            return containsKey(attributeID) ? remove(attributeID).accessor : null;
        }


    }

    private static final class HttpAttributesModel extends ThreadSafeObject implements AttributesModel, AttributeSupport{
        private final KeyedObjects<String, HttpAttributeManager> managers;

        private HttpAttributesModel(){
            this.managers = createManagers();
        }

        private static KeyedObjects<String, HttpAttributeManager> createManagers(){
            return new AbstractKeyedObjects<String, HttpAttributeManager>(10) {
                private static final long serialVersionUID = -1381957593340427015L;

                @Override
                public String getKey(final HttpAttributeManager item) {
                    return item.resourceName;
                }
            };
        }

        @Override
        public void addAttribute(final String resourceName,
                                 final String userDefinedName,
                                 final String attributeName,
                                 final AttributeConnector connector) {
            beginWrite();
            try{
                final HttpAttributeManager manager;
                if(managers.containsKey(resourceName))
                    manager = managers.get(resourceName);
                else managers.put(manager = new HttpAttributeManager(resourceName));
                manager.addAttribute(userDefinedName, connector);
            }
            catch (final JMException e){
                HttpAdapterHelpers.log(Level.SEVERE, "Unable to register attribute %s:%s", resourceName, userDefinedName, e);
            }
            finally {
                endWrite();
            }
        }

        @Override
        public AttributeAccessor removeAttribute(final String resourceName,
                                                 final String userDefinedName,
                                                 final String attributeName) {
            beginWrite();
            try{
                final HttpAttributeManager manager;
                if(managers.containsKey(resourceName))
                    manager = managers.get(resourceName);
                else return null;
                final AttributeAccessor result = manager.removeAttribute(userDefinedName);
                if(manager.isEmpty())
                    managers.remove(resourceName);
                return result;
            }
            finally {
                endWrite();
            }
        }

        /**
         * Removes all attributes from this model.
         */
        @Override
        public void clear() {
            beginWrite();
            try {
                for (final HttpAttributeManager manager : managers.values())
                    manager.clear();
                managers.clear();
            }
            finally {
                endWrite();
            }
        }

        /**
         * Determines whether this model is empty.
         *
         * @return {@literal true}, if this model is empty; otherwise, {@literal false}.
         */
        @Override
        public boolean isEmpty() {
            beginRead();
            try {
                return managers.isEmpty();
            }
            finally {
                endRead();
            }
        }

        private <T> T processAttribute(final String resourceName,
                                       final String attributeName,
                                       final Function<HttpAttributeManager, T> handler) throws WebApplicationException{
            beginRead();
            try{
                if(managers.containsKey(resourceName))
                    return handler.apply(managers.get(resourceName));
                else throw new WebApplicationException(new IllegalArgumentException(String.format("Attribute %s:%s doesn't exist", resourceName, attributeName)), Response.Status.NOT_FOUND);
            }
            finally {
                endRead();
            }
        }

        @Override
        public String getAttribute(final String resourceName, final String attributeName) throws WebApplicationException {
            return processAttribute(resourceName, attributeName, new Function<HttpAttributeManager, String>() {
                @Override
                public String apply(final HttpAttributeManager input) {
                    return input.getAtttribute(attributeName);
                }
            });
        }

        @Override
        public void setAttribute(final String resourceName, final String attributeName, final String value) throws WebApplicationException {
            processAttribute(resourceName, attributeName, new Function<HttpAttributeManager, Void>() {
                @Override
                public Void apply(final HttpAttributeManager input) {
                    input.setAttribute(attributeName, value);
                    return null;
                }
            });
        }

        @Override
        public Set<String> getResourceAttributes(final String resourceName) {
            beginRead();
            try{
                if(managers.containsKey(resourceName)){
                    final HttpAttributeManager manager = managers.get(resourceName);
                    return manager.keySet();
                }
                else return ImmutableSet.of();
            }
            finally {
                endRead();
            }
        }

        @Override
        public Set<String> getHostedResources() {
            beginRead();
            try{
                return ImmutableSet.copyOf(managers.keySet());
            }
            finally {
                endRead();
            }
        }
    }

    private static final class NotificationBroadcaster extends JerseyBroadcaster implements InternalBroadcaster {
        private final KeyedObjects<String, MBeanNotificationInfo> notifications;
        private final String resourceName;

        private NotificationBroadcaster(final String resourceName){
            notifications = createNotifs();
            this.resourceName = resourceName;
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

        private static KeyedObjects<String, MBeanNotificationInfo> createNotifs(){
            return new AbstractKeyedObjects<String, MBeanNotificationInfo>(10) {
                private static final long serialVersionUID = 4500795792209189652L;

                @Override
                public String getKey(final MBeanNotificationInfo item) {
                    return item.getNotifTypes()[0];
                }
            };
        }

        private boolean isInitialized(){
            return initialized.get();
        }

        private void handleNotification(final Notification notif, final Gson formatter){
            notif.setSource(resourceName);
            if(isInitialized() && !isDestroyed() && notifications.containsKey(notif.getType()))
                broadcast(formatter.toJson(notif));
        }

        @Override
        public synchronized void destroy() {
            super.destroy();
        }

        private String makeListID(final String userDefinedName){
            return resourceName + "/" + userDefinedName;
        }

        private void addNotification(final String userDefinedName,
                                    final NotificationConnector connector) throws JMException {
            final String listID = makeListID(userDefinedName);
            notifications.put(connector.enable(listID));
        }

        private MBeanNotificationInfo removeNotification(final String userDefinedName) {
            final String listID = makeListID(userDefinedName);
            return notifications.remove(listID);
        }

        private boolean isEmpty(){
            return notifications.isEmpty();
        }
    }

    private static final class HttpNotificationsModel extends ThreadSafeObject implements NotificationsModel, NotificationSupport{
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
            };
        }

        @Override
        public void addNotification(final String resourceName,
                                    final String userDefinedName,
                                    final String category,
                                    final NotificationConnector connector) {
            beginWrite();
            try{
                final NotificationBroadcaster broadcaster;
                if(notifications.containsKey(resourceName))
                    broadcaster = notifications.get(resourceName);
                else notifications.put(broadcaster = new NotificationBroadcaster(resourceName));
                broadcaster.addNotification(userDefinedName,
                        connector);
            }
            catch (final JMException e){
                HttpAdapterHelpers.log(Level.SEVERE, "Failed to enable notifications for %s resource", resourceName, e);
            }
            finally {
                endWrite();
            }
        }

        @Override
        public NotificationBroadcaster getBroadcaster(final String resourceName) {
            beginRead();
            try{
                return notifications.get(resourceName);
            }
            finally {
                endRead();
            }
        }

        @Override
        public MBeanNotificationInfo removeNotification(final String resourceName,
                                                        final String userDefinedName,
                                                        final String category) {
            beginWrite();
            try{
                final NotificationBroadcaster broadcaster;
                if(notifications.containsKey(resourceName))
                    broadcaster = notifications.get(resourceName);
                else return null;
                final MBeanNotificationInfo metadata = broadcaster.removeNotification(userDefinedName);
                if(broadcaster.isEmpty())
                    notifications.remove(resourceName);
                return metadata;
            }
            finally {
                endWrite();
            }
        }

        /**
         * Removes all notifications from this model.
         */
        @Override
        public void clear() {
            beginWrite();
            try{
                notifications.clear();
            }
            finally {
                endWrite();
            }
        }

        /**
         * Determines whether this model is empty.
         *
         * @return {@literal true}, if this model is empty; otherwise, {@literal false}.
         */
        @Override
        public boolean isEmpty() {
            beginRead();
            try{
                return notifications.isEmpty();
            }
            finally {
                endRead();
            }
        }

        /**
         * Invoked when a JMX notification occurs.
         * The implementation of this method should return as soon as possible, to avoid
         * blocking its notification broadcaster.
         *
         * @param notification The notification.
         * @param handback     An opaque object which helps the listener to associate
         *                     information regarding the MBean emitter. This object is passed to the
         *                     addNotificationListener call and resent, without modification, to the
         */
        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            beginRead();
            try{
                for(final NotificationBroadcaster broadcaster: notifications.values())
                    broadcaster.handleNotification(notification, formatter);
            }
            finally {
                endRead();
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

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @param parameters Adapter startup parameters.
     * @throws Exception Unable to start adapter.
     * @see #populateModel(com.itworks.snamp.adapters.AbstractResourceAdapter.AttributesModel)
     * @see #populateModel(com.itworks.snamp.adapters.AbstractResourceAdapter.NotificationsModel)
     */
    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        populateModel(servletFactory.attributes);
        populateModel(servletFactory.notifications);
        final AtmosphereObjectFactoryBuilder objectFactory = new AtmosphereObjectFactoryBuilder()
                .add(Servlet.class, servletFactory);
        //register RestAdapterServlet as a OSGi service
        publisher.registerServlet(getServletContext(), new AtmosphereServletBridge(objectFactory.build()), null, null);
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @throws Exception Unable to stop adapter.
     * @see #clearModel(com.itworks.snamp.adapters.AbstractResourceAdapter.AttributesModel)
     * @see #clearModel(com.itworks.snamp.adapters.AbstractResourceAdapter.NotificationsModel)
     */
    @Override
    protected void stop() throws Exception {
        //unregister RestAdapter Servlet as a OSGi service.
        publisher.unregister(getServletContext());
        clearModel(servletFactory.attributes);
        clearModel(servletFactory.notifications);
    }

    @Override
    protected void resourceAdded(final String resourceName) {
        try {
            enlargeModel(resourceName, servletFactory.attributes);
            enlargeModel(resourceName, servletFactory.notifications);
        } catch (final JMException e) {
            HttpAdapterHelpers.log(Level.SEVERE, String.format("Unable to process a new resource %s", resourceName), e);
        }
    }

    @Override
    protected void resourceRemoved(final String resourceName) {
        clearModel(resourceName, servletFactory.attributes);
        clearModel(resourceName, servletFactory.notifications);
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
