package com.itworks.snamp.adapters.http;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.adapters.*;
import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.json.Formatters;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.jersey.JerseyBroadcaster;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.management.*;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
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
        private final Gson formatter;

        private HttpAttributeMapping(final MBeanAttributeInfo attributeInfo) {
            super(attributeInfo);
            final String dateFormat = HttpAdapterConfigurationDescriptor.parseDateFormatParam(getMetadata().getDescriptor());
            GsonBuilder builder = new GsonBuilder();
            if (dateFormat != null && dateFormat.length() > 0)
                builder = builder.setDateFormat(dateFormat);
            builder = Formatters.enableBufferSupport(builder);
            builder = Formatters.enableOpenTypeSystemSupport(builder);
            formatter = builder
                    .serializeSpecialFloatingPointValues()
                    .serializeNulls().create();
        }

        @Override
        protected String interceptGet(final Object value) {
            return formatter.toJson(value);
        }

        @Override
        protected Object interceptSet(final Object value) throws InterceptionException {
            if (getType() != null && value instanceof String)
                return formatter.fromJson((String) value, getType().getJavaType());
            else throw new InterceptionException(new IllegalArgumentException("String expected"));
        }
    }

    private static final class HttpAttributesModel extends AbstractAttributesModel<HttpAttributeMapping> implements AttributeSupport{

        @Override
        public String getAttribute(final String resourceName, final String attributeName) throws WebApplicationException {
            try {
                return Objects.toString(getAttributeValue(resourceName, attributeName));
            } catch (final AttributeNotFoundException e) {
                throw new WebApplicationException(e, Response.Status.NOT_FOUND);
            } catch (final ReflectionException | MBeanException e) {
                throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        @Override
        public void setAttribute(final String resourceName, final String attributeName, final String value) throws WebApplicationException {
            try {
                setAttributeValue(resourceName, attributeName, value);
            } catch (final AttributeNotFoundException e) {
                throw new WebApplicationException(e, Response.Status.NOT_FOUND);
            } catch (final AttributeAccessor.InterceptionException e) {
                throw new WebApplicationException(e, METHOD_NOT_ALLOWED);
            } catch (final InvalidAttributeValueException e) {
                throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
            } catch (final MBeanException | ReflectionException e) {
                throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        @Override
        protected HttpAttributeMapping createAccessor(final MBeanAttributeInfo metadata) throws Exception {
            return new HttpAttributeMapping(metadata);
        }
    }

    private static final class HttpNotificationRouter extends UnicastNotificationRouter {
        private final String resourceName;

        private HttpNotificationRouter(final String resourceName,
                                       final MBeanNotificationInfo metadata,
                                       final NotificationListener destination) {
            super(metadata, destination);
            this.resourceName = resourceName;
        }

        @Override
        protected Notification intercept(final Notification notification) {
            notification.setSource(resourceName);
            return notification;
        }
    }

    private static final class NotificationBroadcaster extends JerseyBroadcaster implements InternalBroadcaster, NotificationListener {
        private final ResourceNotificationList<HttpNotificationRouter> notifications;
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
        public void initialize(final HttpServletRequest request) throws URISyntaxException {
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
        public void handleNotification(final NotificationEvent event){
            if(isInitialized() && !isDestroyed())
                broadcast(formatter.toJson(event.getNotification()));
        }

        @Override
        public void destroy() {
            if (isInitialized()) super.destroy();
        }

        private UnicastNotificationRouter addNotification(final MBeanNotificationInfo metadata) {
            final HttpNotificationRouter emitter = new HttpNotificationRouter(resourceName,
                    metadata,
                    this);
            notifications.put(emitter);
            return emitter;
        }

        private UnicastNotificationRouter removeNotification(final MBeanNotificationInfo metadata) {
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
        private static final Gson FORMATTER = Formatters.enableAll(new GsonBuilder())
                .serializeSpecialFloatingPointValues()
                .serializeNulls()
                .create();

        private HttpNotificationsModel(){
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

        private UnicastNotificationRouter addNotification(final String resourceName,
                                    final MBeanNotificationInfo metadata) {
            try (final LockScope ignored = beginWrite()) {
                final NotificationBroadcaster broadcaster;
                if (notifications.containsKey(resourceName))
                    broadcaster = notifications.get(resourceName);
                else notifications.put(broadcaster = new NotificationBroadcaster(resourceName, FORMATTER));
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
