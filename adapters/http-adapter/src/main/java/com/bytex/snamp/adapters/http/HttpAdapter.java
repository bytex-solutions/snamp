package com.bytex.snamp.adapters.http;

import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.adapters.*;
import com.bytex.snamp.adapters.NotificationListener;
import com.bytex.snamp.adapters.modeling.*;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.internal.RecordReader;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.json.Formatters;
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

import static org.atmosphere.cpr.FrameworkConfig.ATMOSPHERE_CONFIG;

/**
 * Represents HTTP adapter that exposes management information through HTTP and WebSocket to the outside world.
 * This class cannot be inherited.
 * @author Roman Sakno
 */
final class HttpAdapter extends AbstractResourceAdapter {
    private static final int METHOD_NOT_ALLOWED = 405;
    private static final String SERVLET_CONTEXT = "/snamp/adapters/http/%s";

    private static final class HttpModelOfAttributes extends ModelOfAttributes<HttpAttributeAccessor> implements AttributeSupport{

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
        protected HttpAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) throws Exception {
            return new HttpAttributeAccessor(metadata);
        }
    }

    private static final class NotificationBroadcaster extends JerseyBroadcaster implements InternalBroadcaster, NotificationListener {
        private final ResourceNotificationList<HttpNotificationAccessor> notifications;
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

        private NotificationRouter addNotification(final MBeanNotificationInfo metadata) {
            final HttpNotificationAccessor emitter = new HttpNotificationAccessor(resourceName,
                    metadata,
                    this);
            notifications.put(emitter);
            return emitter;
        }

        private NotificationRouter removeNotification(final MBeanNotificationInfo metadata) {
            return notifications.remove(metadata);
        }

        private boolean isEmpty(){
            return notifications.isEmpty();
        }

        private void clear() {
            for(final NotificationAccessor accessor: notifications.values())
                accessor.close();
            notifications.clear();
        }
    }

    private static final class HttpModelOfNotifications extends ModelOfNotifications<HttpNotificationAccessor> implements NotificationSupport{
        private final KeyedObjects<String, NotificationBroadcaster> notifications;
        private static final Gson FORMATTER = Formatters.enableAll(new GsonBuilder())
                .serializeSpecialFloatingPointValues()
                .serializeNulls()
                .create();

        private HttpModelOfNotifications(){
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

        private NotificationRouter addNotification(final String resourceName,
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
        public <E extends Exception> void forEachNotification(final RecordReader<String, ? super HttpNotificationAccessor, E> notificationReader) throws E {
            try (final LockScope ignored = beginRead()) {
                for (final NotificationBroadcaster broadcaster : notifications.values())
                    for (final HttpNotificationAccessor accessor : broadcaster.notifications.values())
                        notificationReader.read(broadcaster.resourceName, accessor);
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
        private final HttpModelOfNotifications notifications;
        private final HttpModelOfAttributes attributes;

        JerseyServletFactory(final HttpModelOfAttributes attributes, final HttpModelOfNotifications notifications){
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
        servletFactory = new JerseyServletFactory(new HttpModelOfAttributes(),
                new HttpModelOfNotifications());
    }

    private String getServletContext(){
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
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)servletFactory.attributes.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)servletFactory.notifications.addNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) {
        final Collection<? extends AttributeAccessor> attributes =
                servletFactory.attributes.clear(resourceName);
        final Collection<? extends NotificationAccessor> notifications =
                servletFactory.notifications.clear(resourceName);
        return Iterables.concat(attributes, notifications);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)servletFactory.attributes.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)servletFactory.notifications.removeNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanAttributeInfo>> getAttributes(final String servletContext,
                                                                                                    final AttributeSet<HttpAttributeAccessor> attributes){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanAttributeInfo>> result =
                HashMultimap.create();
        attributes.forEachAttribute(new RecordReader<String, HttpAttributeAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final HttpAttributeAccessor accessor) {
                return result.put(resourceName, new ReadOnlyFeatureBindingInfo<MBeanAttributeInfo>(accessor,
                        "path", accessor.getPath(servletContext, resourceName),
                        FeatureBindingInfo.MAPPED_TYPE, accessor.getJsonType()
                ));
            }
        });
        return result;
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanNotificationInfo>> getNotifications(final String servletContext,
                                                                                                          final NotificationSet<HttpNotificationAccessor> notifs){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanNotificationInfo>> result =
                HashMultimap.create();
        notifs.forEachNotification(new RecordReader<String, HttpNotificationAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final HttpNotificationAccessor accessor) {
                return result.put(resourceName, new ReadOnlyFeatureBindingInfo<MBeanNotificationInfo>(accessor,
                            "path", accessor.getPath(servletContext, resourceName)
                        ));
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        if(featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getAttributes(getServletContext(), servletFactory.attributes);
        else if(featureType.isAssignableFrom(MBeanNotificationInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getNotifications(getServletContext(), servletFactory.notifications);
        else return super.getBindings(featureType);
    }
}