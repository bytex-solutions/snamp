package com.bytex.snamp.gateway.http;

import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents HTTP gateway that exposes management information through HTTP and WebSocket to the outside world.
 * This class cannot be inherited.
 * @author Roman Sakno
 */
final class HttpGateway extends AbstractGateway {
    private static final String ROOT_CONTEXT = "/snamp/gateway/http/%s";

    private final HttpService publisher;
    private final HttpModelOfAttributes attributes;
    private final HttpModelOfNotifications notifications;

    HttpGateway(final String instanceName, final HttpService servletPublisher) {
        super(instanceName);
        publisher = Objects.requireNonNull(servletPublisher, "servletPublisher is null.");
        attributes = new HttpModelOfAttributes();
        notifications = new HttpModelOfNotifications();
    }

    private String getServletContextForAttributes(){
        return String.format(ROOT_CONTEXT, instanceName);
    }

    private String getServletContextForNotifications(){
        return String.format(ROOT_CONTEXT + "/notifications", instanceName);
    }

    @Override
    protected void start(final Map<String, String> parameters) throws ServletException, NamespaceException {
        //register HttpGatewayServlet as a OSGi service. This service will be captured by Jetty installed in underlying OSGi environment
        publisher.registerServlet(getServletContextForAttributes(), new AttributeAccessServlet(attributes), new Hashtable<>(), null);
        publisher.registerServlet(getServletContextForNotifications(), new NotificationAccessServlet(notifications), new Hashtable<>(), null);
    }

    @Override
    protected void stop() {
        //unregister HttpGatewayServlet Servlet from OSGi registry
        publisher.unregister(getServletContextForAttributes());
        publisher.unregister(getServletContextForNotifications());
        attributes.clear();
        notifications.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)attributes.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)notifications.addNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) {
        final Collection<? extends AttributeAccessor> attributes = this.attributes.clear(resourceName);
        final Collection<? extends NotificationAccessor> notifications = this.notifications.clear(resourceName);
        return Stream.concat(attributes.stream(), notifications.stream());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)attributes.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)notifications.removeNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanAttributeInfo>> getAttributes(final String servletContext,
                                                                                                    final AttributeSet<HttpAttributeAccessor> attributes){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanAttributeInfo>> result =
                HashMultimap.create();
        attributes.forEachAttribute((resourceName, accessor) -> result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor,
                "path", accessor.getPath(servletContext, resourceName),
                FeatureBindingInfo.MAPPED_TYPE, accessor.getJsonType()
        )));
        return result;
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanNotificationInfo>> getNotifications(final String servletContext,
                                                                                                          final NotificationSet<HttpNotificationAccessor> notifs){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanNotificationInfo>> result =
                HashMultimap.create();
        notifs.forEachNotification( (resourceName, accessor) -> result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor,
                "path", accessor.getPath(servletContext, resourceName)
        )));
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        if(featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getAttributes(getServletContextForAttributes(), attributes);
        else if(featureType.isAssignableFrom(MBeanNotificationInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getNotifications(getServletContextForNotifications(), notifications);
        else return super.getBindings(featureType);
    }
}
