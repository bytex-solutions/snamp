package com.itworks.snamp.adapters.http;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.jmx.WellKnownType;
import org.atmosphere.jersey.JerseyBroadcaster;
import org.osgi.service.http.HttpService;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.nio.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents HTTP adapter that exposes management information through HTTP and WebSocket to the outside world.
 * This class cannot be inherited.
 * @author Roman Sakno
 */
final class HttpAdapter extends AbstractResourceAdapter {
    static final String NAME = HttpAdapterHelpers.ADAPTER_NAME;
    private static final int METHOD_NOT_ALLOWED = 405;

    private final HttpAttributesModel attributes;
    private final HttpService publisher;
    private final HttpNotificationsModel notifications;

    HttpAdapter(final String instanceName, final HttpService servletPublisher){
        super(instanceName);
        attributes = new HttpAttributesModel(instanceName);
        notifications = new HttpNotificationsModel(instanceName);
        this.publisher = Objects.requireNonNull(servletPublisher, "servletPublisher is null.");
    }

    private String getServletContext(){
        final String SERVLET_CONTEXT = "/snamp/adapters/%s/http";
        return String.format(SERVLET_CONTEXT, getInstanceName());
    }

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
        private static String ATTR_NAME_SPLITTER = "/";

        private HttpAttributeManager(){
            super(10);
        }

        @Override
        public String getKey(final HttpAttributeMapping item) {
            return item.getName();
        }

        private static String makeAttributeID(final String adapterInstanceName,
                                              final String attributeName){
            return adapterInstanceName + ATTR_NAME_SPLITTER + attributeName;
        }

        private Set<String> getResourceAttributes(final String adapterInstanceName){
            final Set<String> attributes = Sets.newHashSetWithExpectedSize(size());
            for(final String attributeID: keySet())
                attributes.add(attributeID.replaceFirst(adapterInstanceName + ATTR_NAME_SPLITTER, ""));
            return attributes;
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

        private String getAtttribute(final String adapterInstanceName, final String attributeName) throws WebApplicationException{
            final String attributeID;
            if(containsKey(attributeID = makeAttributeID(adapterInstanceName, attributeName))){
                final HttpAttributeMapping mapping = get(attributeID);
                if(mapping.canRead())
                    return mapping.toString(mapping.getValue());
                else throw new WebApplicationException(new IllegalStateException(String.format("Attribute %s is write-ony", attributeName)), METHOD_NOT_ALLOWED);
            }
            else throw new WebApplicationException(new IllegalArgumentException(String.format("Attribute %s doesn't exist", attributeName)), Response.Status.NOT_FOUND);
        }

        private void setAttribute(final String adapterInstanceName,
                                  final String attributeName,
                                  final String value) {
            final String attributeID;
            if(containsKey(attributeID = makeAttributeID(adapterInstanceName, attributeName))){
                final HttpAttributeMapping mapping = get(attributeID);
                if(mapping.canWrite())
                    mapping.setValue(mapping.fromString(value));
                else throw new WebApplicationException(new IllegalStateException(String.format("Attribute %s is read-only", attributeName)), METHOD_NOT_ALLOWED);
            }
            else throw new WebApplicationException(new IllegalArgumentException(String.format("Attribute %s doesn't exist", attributeName)), Response.Status.NOT_FOUND);
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
                        builder = builder.registerTypeAdapter(type.getType(), new ByteBufferFormatter());
                    break;
                    case CHAR_BUFFER:
                        builder = builder.registerTypeAdapter(type.getType(), new CharBufferFormatter());
                    break;
                    case SHORT_BUFFER:
                        builder = builder.registerTypeAdapter(type.getType(), new ShortBufferFormatter());
                    break;
                    case INT_BUFFER:
                        builder = builder.registerTypeAdapter(type.getType(), new IntBufferFormatter());
                    break;
                    case LONG_BUFFER:
                        builder = builder.registerTypeAdapter(type.getType(), new LongBufferFormatter());
                    break;
                    case FLOAT_BUFFER:
                        builder = builder.registerTypeAdapter(type.getType(), new FloatBufferFormatter());
                    break;
                    case DOUBLE_BUFFER:
                        builder = builder.registerTypeAdapter(type.getType(), new DoubleBufferFormatter());
                    break;
                    case OBJECT_NAME_ARRAY:
                    case OBJECT_NAME:
                        builder = builder.registerTypeAdapter(ObjectName.class, new ObjectNameFormatter());
                    break;
                    case DICTIONARY:
                        final CompositeType compositeType = (CompositeType)accessor.getOpenType();
                        builder = builder.registerTypeHierarchyAdapter(CompositeType.class, new CompositeDataFormatter(compositeType));
                    break;
                    case TABLE:
                        final TabularType tabularType = (TabularType)accessor.getOpenType();
                        builder = builder.registerTypeHierarchyAdapter(TabularType.class, new TabularDataFormatter(tabularType));
                    break;
                }
            return new HttpAttributeMapping(accessor, builder);
        }

        private void addAttribute(final String adapterInstanceName,
                                  final String attributeName,
                                  final AttributeConnector connector) throws JMException {
            final String attributeID = makeAttributeID(adapterInstanceName, attributeName);
            put(createAttribute(connector.connect(attributeID)));
        }

        private AttributeAccessor removeAttribute(final String adapterInstanceName, final String attributeName) {
            final String attributeID = makeAttributeID(adapterInstanceName, attributeName);
            return containsKey(attributeID) ? remove(attributeID).accessor : null;
        }


    }

    private static final class HttpAttributesModel extends ThreadSafeObject implements AttributesModel, AttributeSupport{
        private final String adapterInstanceName;
        private final Map<String, HttpAttributeManager> managers;

        private HttpAttributesModel(final String adapterInstanceName){
            this.adapterInstanceName = adapterInstanceName;
            this.managers = new HashMap<>(10);
        }

        @Override
        public void addAttribute(final String resourceName,
                                 final String attributeName,
                                 final AttributeConnector connector) {
            beginWrite();
            try{
                final HttpAttributeManager manager;
                if(managers.containsKey(resourceName))
                    manager = managers.get(resourceName);
                else managers.put(resourceName, manager = new HttpAttributeManager());
                manager.addAttribute(adapterInstanceName, attributeName, connector);
            }
            catch (final JMException e){
                HttpAdapterHelpers.log(Level.SEVERE, String.format("Unable to register attribute %s:%s", resourceName, attributeName), e);
            }
            finally {
                endWrite();
            }
        }

        @Override
        public AttributeAccessor removeAttribute(final String resourceName,
                                                 final String attributeName) {
            beginWrite();
            try{
                final HttpAttributeManager manager;
                if(managers.containsKey(resourceName))
                    manager = managers.get(resourceName);
                else return null;
                final AttributeAccessor result = manager.removeAttribute(adapterInstanceName, attributeName);
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
                    return input.getAtttribute(adapterInstanceName, attributeName);
                }
            });
        }

        @Override
        public void setAttribute(final String resourceName, final String attributeName, final String value) throws WebApplicationException {
            processAttribute(resourceName, attributeName, new Function<HttpAttributeManager, Void>() {
                @Override
                public Void apply(final HttpAttributeManager input) {
                    input.setAttribute(adapterInstanceName, attributeName, value);
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
                    return manager.getResourceAttributes(adapterInstanceName);
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

    private static final class NotificationBroadcaster extends JerseyBroadcaster{
        private final KeyedObjects<String, MBeanNotificationInfo> notifications;

        private NotificationBroadcaster(){
            notifications = createNotifs();
        }

        private static KeyedObjects<String, MBeanNotificationInfo> createNotifs(){
            return new AbstractKeyedObjects<String, MBeanNotificationInfo>(10) {
                @Override
                public String getKey(final MBeanNotificationInfo item) {
                    return item.getNotifTypes()[0];
                }
            };
        }

        private void handleNotification(final Notification notif, final Gson formatter){
            if(notifications.containsKey(notif.getType()))
                broadcast(formatter.toJson(notif));
        }

        private static String makeListID(final String adapterInstanceName,
                                         final String category){
            return adapterInstanceName + "/" + category;
        }

        private void addNotification(final String adapterInstanceName,
                                    final String category,
                                    final NotificationConnector connector) throws JMException {
            final String listID = makeListID(adapterInstanceName, category);
            notifications.put(connector.enable(listID));
        }

        private MBeanNotificationInfo removeNotification(final String adapterInstanceName,
                                                        final String category) {
            final String listID = makeListID(adapterInstanceName, category);
            return notifications.remove(listID);
        }

        private boolean isEmpty(){
            return notifications.isEmpty();
        }
    }

    private static final class HttpNotificationsModel extends ThreadSafeObject implements NotificationsModel, NotificationSupport{
        private final Map<String, NotificationBroadcaster> notifications;
        private final Gson formatter;
        private final String adapterInstanceName;

        private HttpNotificationsModel(final String adapterInstanceName){
            this.adapterInstanceName = adapterInstanceName;
            this.formatter = new GsonBuilder()
                    .registerTypeHierarchyAdapter(Notification.class, new NotificationJsonSerializer())
                    .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter())
                    .registerTypeAdapter(ByteBuffer.class, new ByteBufferFormatter())
                    .registerTypeAdapter(CharBuffer.class, new CharBufferFormatter())
                    .registerTypeAdapter(ShortBuffer.class, new ShortBufferFormatter())
                    .registerTypeAdapter(IntBuffer.class, new IntBufferFormatter())
                    .registerTypeAdapter(LongBuffer.class, new LongBufferFormatter())
                    .registerTypeAdapter(FloatBuffer.class, new FloatBufferFormatter())
                    .registerTypeAdapter(DoubleBuffer.class, new DoubleBufferFormatter())
                    .registerTypeHierarchyAdapter(CompositeData.class, new CompositeDataJsonSerializer())
                    .registerTypeHierarchyAdapter(TabularData.class, new TabularDataJsonSerializer())
                    .create();
            this.notifications = new HashMap<>(10);
        }

        /**
         * Registers a new notification in this model.
         *
         * @param resourceName The name of the resource that supplies the specified notification.
         * @param category     The notification category.
         * @param connector    The notification connector.
         */
        @Override
        public void addNotification(final String resourceName,
                                    final String category,
                                    final NotificationConnector connector) {
            beginWrite();
            try{
                final NotificationBroadcaster broadcaster;
                if(notifications.containsKey(resourceName))
                    broadcaster = notifications.get(resourceName);
                else notifications.put(resourceName, broadcaster = new NotificationBroadcaster());
                broadcaster.addNotification(adapterInstanceName,
                        category,
                        connector);
            }
            catch (final JMException e){
                HttpAdapterHelpers.log(Level.SEVERE, String.format("Failed to enable notifications for %s resource", resourceName), e);
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

        /**
         * Removes the notification from this model.
         *
         * @param resourceName The name of the resource that supplies the specified notification.
         * @param category     The notification category.
         * @return The enabled notification removed from this model.
         */
        @Override
        public MBeanNotificationInfo removeNotification(final String resourceName, final String category) {
            beginWrite();
            try{
                final NotificationBroadcaster broadcaster;
                if(notifications.containsKey(resourceName))
                    broadcaster = notifications.get(resourceName);
                else return null;
                final MBeanNotificationInfo metadata = broadcaster.removeNotification(adapterInstanceName, category);
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
        populateModel(attributes);
        populateModel(notifications);
        //register RestAdapterServlet as a OSGi service
        publisher.registerServlet(getServletContext(), new HttpAdapterServlet(attributes, notifications), null, null);
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
        clearModel(attributes);
        clearModel(notifications);
    }

    @Override
    protected void resourceAdded(final String resourceName) {
        try {
            enlargeModel(resourceName, attributes);
            enlargeModel(resourceName, notifications);
        } catch (final JMException e) {
            HttpAdapterHelpers.log(Level.SEVERE, String.format("Unable to process a new resource %s", resourceName), e);
        }
    }

    @Override
    protected void resourceRemoved(final String resourceName) {
        clearModel(resourceName, attributes);
        clearModel(resourceName, notifications);
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
